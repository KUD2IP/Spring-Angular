package com.example.book_back.auth;

import com.example.book_back.email.EmailService;
import com.example.book_back.email.EmailTemplateName;
import com.example.book_back.role.Role;
import com.example.book_back.role.RoleRepository;
import com.example.book_back.security.JwtService;
import com.example.book_back.user.Token;
import com.example.book_back.user.TokenRepository;
import com.example.book_back.user.User;
import com.example.book_back.user.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;


    /**
     * Регистрация нового пользователя.
     *
     * @param request запрос на регистрацию
     * @throws MessagingException если произошла ошибка при отправке электронной почты
     */
    public void register(RegistrationRequest request) throws MessagingException {
        // Получаем роль пользователя
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Роль пользователя не задана"));

        // Создаем нового пользователя
        User user = User.builder()
                .firstName(request.getFirstName()) // Имя пользователя
                .lastName(request.getLastName()) // Фамилия пользователя
                .email(request.getEmail()) // Электронная почта пользователя
                .password(passwordEncoder.encode(request.getPassword())) // Хэш пароля пользователя
                .accountLocked(false) // Аккаунт пользователя не заблокирован
                .enabled(false) // Аккаунт пользователя не актирован
                .roles(List.of(userRole)) // Роль пользователя
                .build();

        // Сохраняем пользователя в базе данных
        userRepository.save(user);

        // Отправляем электронную почту для активации аккаунта
        sendValidationEmail(user);
    }

    /**
     * Отправляет электронное письмо с подтверждением аккаунта пользователю.
     *
     * @param user пользователь, которому отправляется письмо
     * @throws MessagingException если произошла ошибка при отправке электронной почты
     */
    private void sendValidationEmail(User user) throws MessagingException {
        // Генерируем и сохраняем токен активации
        String newToken = generateAndSaveActivationToken(user);

        // Отправляем электронное письмо
        emailService.sendEmail(
                user.getEmail(), // Электронная почта получателя
                user.getFullName(), // Полное имя получателя
                EmailTemplateName.ACTIVATE_ACCOUNT, // Тип шаблона электронного письма
                activationUrl, // URL для активации аккаунта
                newToken, // Сгенерированный токен активации
                "Активация аккаунта" // Тема письма
        );
    }

    /**
     * Генерирует и сохраняет токен активации для пользователя.
     *
     * @param user пользователь, для которого генерируется токен активации
     * @return сгенерированный токен активации
     * @throws MessagingException если произошла ошибка при сохранении токена в репозитории
     */
    private String generateAndSaveActivationToken(User user) throws MessagingException {
        // Генерируем случайную строку длиной 6 символов для токена активации
        String generatedToken = generateActivationCode(6);

        // Создаем новый объект токена с заданными параметрами
        Token token = Token.builder()
                .token(generatedToken) // Устанавливаем сгенерированный токен
                .createdAt(LocalDateTime.now()) // Устанавливаем текущую дату и время создания токена
                .expiresAt(LocalDateTime.now().plusMinutes(15)) // Устанавливаем дату и время истечения срока действия токена
                .user(user) // Устанавливаем пользователя, для которого генерируется токен
                .build();

        // Сохраняем токен в репозитории
        tokenRepository.save(token);

        // Возвращаем сгенерированный токен активации
        return generatedToken;
    }

    /**
     * Генерирует случайную строку длины `length` из символов '0' - '9'.
     *
     * @param length длина генерируемой строки
     * @return случайная строка длиной `length`
     */
    private String generateActivationCode(int length) {
        // Список символов, из которых будет генерироваться строка
        String characters = "0123456789";

        // Строковый буфер для хранения генерируемой строки
        StringBuilder codeBuilder = new StringBuilder();

        // Генератор случайных чисел
        SecureRandom secureRandom = new SecureRandom();

        // Генерируем `length` символов и добавляем их в строку
        for (int i = 0; i < length; i++) {
            // Выбираем случайный индекс символа из списка `characters`
            int randomIndex = secureRandom.nextInt(characters.length());
            // Добавляем символ по выбранному индексу в строку
            codeBuilder.append(characters.charAt(randomIndex));
        }

        // Возвращаем сгенерированную строку
        return codeBuilder.toString();
    }

    /**
     * Метод для аутентификации пользователя на основе предоставленных учетных данных.
     *
     * @param request объект с данными аутентификации
     * @return объект ответа на аутентификацию с JWT-токеном
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // Выполняем аутентификацию пользователя
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Создаем мапу для хранения утверждений о пользователе
        var claims = new HashMap<String, Object>();

        // Получаем пользователя из успешной аутентификации
        User user = ((User) auth.getPrincipal());

        // Добавляем полное имя пользователя в мапу утверждений
        claims.put("fullName", user.getFullName());

        // Генерируем JWT-токен на основе утверждений и пользователя
        String jwtToken = jwtService.generateToken(claims, user);

        // Возвращаем объект ответа на аутентификацию с генерированным токеном
        return AuthenticationResponse.builder()
                .token(jwtToken).build();
    }


    /**
     * Метод активации учетной записи пользователя.
     *
     * @param token Токен активации, отправленный пользователю на почту.
     * @throws MessagingException Если произошла ошибка при отправке письма.
     */
    //    @Transactional
    public void activateAccount(String token) throws MessagingException {
        // Ищем токен активации в репозитории
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Неверный токен"));

        // Проверяем, не истек ли срок действия токена
        if(LocalDateTime.now().isAfter(savedToken.getExpiresAt())){
            // Отправляем письмо для повторной активации
            sendValidationEmail(savedToken.getUser());

            // Выбрасываем исключение, уведомляя пользователя
            throw new RuntimeException("Токен активации истек. На вашу почту отправлено новое письмо.");
        }

        // Ищем пользователя по ID из токена
        User user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        // Активируем учетную запись пользователя
        user.setEnabled(true);

        // Сохраняем изменения в репозитории
        userRepository.save(user);

        // Обновляем дату и время активации токена
        savedToken.setValidatedAt(LocalDateTime.now());

        // Сохраняем изменения в репозитории
        tokenRepository.save(savedToken);
    }
}

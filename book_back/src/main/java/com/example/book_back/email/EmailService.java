package com.example.book_back.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;


    /**
     * Асинхронно отправляет электронное письмо с помощью указанных параметров.
     *
     * @param to адрес получателя
     * @param username имя пользователя, которое будет использоваться в шаблоне письма
     * @param emailTemplate имя шаблона письма, если null, будет использоваться шаблон по умолчанию
     * @param confirmationUrl URL подтверждения
     * @param activationCode код активации
     * @param subject тема письма
     * @throws MessagingException если возникает ошибка при отправке письма
     */
    @Async
    public void sendEmail(
            String to,
            String username,
            EmailTemplateName emailTemplate,
            String confirmationUrl,
            String activationCode,
            String subject
    ) throws MessagingException {
        // Определяем имя шаблона письма
        String templateName;
        if(emailTemplate == null){
            templateName = "confirm-email";
        }else{
            templateName = emailTemplate.name();
        }

        // Создаем объект MimeMessage для отправки письма
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        // Инициализируем помощника для работы с MimeMessage
        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage,
                MULTIPART_MODE_MIXED,
                UTF_8.name()
        );

        // Создаем карту свойств, которые будут использованы в шаблоне письма
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", username);
        properties.put("confirmationUrl", confirmationUrl);
        properties.put("activation_code", activationCode);

        // Создаем объект Context для работы с шаблоном письма
        Context context = new Context();
        context.setVariables(properties);

        // Устанавливаем отправителя и получателя письма
        helper.setFrom("Storozhev@spring.com");
        helper.setTo(to);

        // Устанавливаем тему письма
        helper.setSubject(subject);

        // Обрабатываем шаблон письма и устанавливаем его в письмо
        String template = templateEngine.process(templateName, context);
        helper.setText(template, true);

        // Отправляем письмо
        mailSender.send(mimeMessage);
    }
}

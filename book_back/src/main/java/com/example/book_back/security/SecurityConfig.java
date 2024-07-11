package com.example.book_back.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final JwtFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    /**
     * Метод настраивает фильтры безопасности для HTTP-запросов.
     *
     * @param http объект HttpSecurity, который будет настроен
     * @return SecurityFilterChain, который содержит настроенные фильтры безопасности
     * @throws Exception если возникает ошибка при настройке фильтров
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Разрешаем CORS
                .cors(withDefaults())

                // Отключаем CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // Настраиваем авторизацию запросов
                .authorizeHttpRequests(req -> req
                // Разрешаем доступ к следующим путям без аутентификации
                .requestMatchers(
                        "/auth/**",
                        "v2/api-docs",
                        "v3/api-docs",
                        "v3/api-docs/**",
                        "swagger-resources",
                        "swagger-resources/**",
                        "configuration/ui",
                        "configuration/security",
                        "swagger-ui/**",
                        "webjars/**",
                        "swagger-ui.html"
                )
                        .permitAll()
                // Требуем аутентификацию для всех остальных запросов
                .anyRequest()
                        .authenticated()
            )

            // Устанавливаем политику создания сессий
            .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))

            // Устанавливаем провайдера аутентификации
            .authenticationProvider(authenticationProvider)

            // Добавляем фильтр JWTAuthFilter перед UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

package com.example.book_back.config;

import com.example.book_back.user.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class ApplicationAuditAware implements AuditorAware<Long> {

    /**
     * Возвращает идентификатор текущего аудитора, если он аутентифицирован и не является анонимным.
     *
     * @return идентификатор текущего аудитора, если он аутентифицирован и не является анонимным, иначе пустой Optional
     */
    @Override
    public Optional<Long> getCurrentAuditor() {
        // Получение текущей аутентификации из контекста безопасности
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Проверка, если аутентификация не существует, не аутентифицирована или является анонимной, то возвращаем пустой Optional
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        // Получение пользователя из принципала аутентификации
        User userPrincipal = (User) authentication.getPrincipal();

        // Возвращаем идентификатор пользователя
        return Optional.ofNullable(userPrincipal.getId());
    }
}


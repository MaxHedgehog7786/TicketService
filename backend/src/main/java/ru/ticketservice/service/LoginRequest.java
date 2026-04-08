package ru.ticketservice.service;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @brief DTO запроса на вход в систему.
 *
 * Используется эндпоинтом {@code POST /auth/login}.
 */
@Data
public class LoginRequest {

    /** @brief Логин пользователя. Обязательное поле. */
    @NotBlank String login;

    /** @brief Пароль пользователя. Обязательное поле. */
    @NotBlank String password;
}

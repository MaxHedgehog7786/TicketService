package ru.ticketservice.service;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * @brief DTO запроса на регистрацию нового пользователя.
 *
 * Используется эндпоинтом {@code POST /auth/register}.
 * Все поля, кроме {@code phone} и {@code subscribed}, обязательны.
 */
@Data
public class RegisterRequest {

    /** @brief Желаемый логин (уникальный в системе). */
    @NotBlank String login;

    /** @brief Пароль (минимум 8 символов). */
    @NotBlank @Size(min = 8) String password;

    /** @brief Имя пользователя. */
    @NotBlank String name;

    /** @brief Фамилия пользователя. */
    @NotBlank String surname;

    /** @brief Адрес электронной почты (уникальный в системе). */
    @NotBlank @Email String email;

    /** @brief Номер телефона (необязательно). */
    String phone;

    /** @brief Согласие на получение еженедельной рассылки. По умолчанию {@code false}. */
    Boolean subscribed = false;
}

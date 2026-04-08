package ru.ticketservice.service;

import lombok.*;

/**
 * @brief DTO ответа на запросы аутентификации (вход / регистрация).
 *
 * Возвращается клиенту при успешном входе или регистрации.
 * Содержит JWT-токен и основные данные пользователя для кеширования
 * в клиентском хранилище (Zustand store).
 */
@Data @AllArgsConstructor
public class AuthResponse {

    /** @brief Уникальный идентификатор пользователя в базе данных. */
    Integer id;

    /** @brief Подписанный JWT-токен для последующих запросов. */
    String token;

    /** @brief Логин пользователя. */
    String login;

    /** @brief Имя пользователя для отображения в интерфейсе. */
    String name;

    /** @brief Роль пользователя: {@code USER} или {@code ADMIN}. */
    String role;
}

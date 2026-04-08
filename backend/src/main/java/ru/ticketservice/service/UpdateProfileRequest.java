package ru.ticketservice.service;

import lombok.*;

/**
 * @brief DTO запроса на обновление профиля пользователя.
 *
 * Используется эндпоинтами {@code PUT /profile} и {@code POST /profile/subscribe}.
 * Поля с {@code null} значением игнорируются — обновляются только переданные данные.
 */
@Data @AllArgsConstructor @NoArgsConstructor
public class UpdateProfileRequest {

    /** @brief Новое имя пользователя. {@code null} — не изменять. */
    String name;

    /** @brief Новая фамилия пользователя. {@code null} — не изменять. */
    String surname;

    /** @brief Новый номер телефона. {@code null} — не изменять. */
    String phone;

    /** @brief Флаг подписки на рассылку. {@code null} — не изменять. */
    Boolean subscribed;
}

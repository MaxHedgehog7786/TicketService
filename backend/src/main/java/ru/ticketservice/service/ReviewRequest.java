package ru.ticketservice.service;

import lombok.*;

/**
 * @brief DTO запроса на создание или обновление отзыва.
 *
 * Используется эндпоинтами:
 * <ul>
 *   <li>{@code POST /reviews} — создание нового отзыва</li>
 *   <li>{@code PUT /reviews/{id}} — обновление существующего отзыва</li>
 * </ul>
 */
@Data
public class ReviewRequest {

    /**
     * @brief Идентификатор мероприятия.
     * Используется только при создании нового отзыва.
     */
    private Integer eventId;

    /** @brief Оценка от 1 до 5. */
    private Short rating;

    /** @brief Текст комментария. */
    private String comment;
}

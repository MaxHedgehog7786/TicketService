package ru.ticketservice.service;

import lombok.*;
import java.time.LocalDateTime;

/**
 * @brief DTO отзыва о мероприятии.
 *
 * Содержит имя автора (денормализовано из сущности User) для
 * отображения на странице мероприятия без дополнительных запросов.
 * Также включает идентификатор автора для проверки прав редактирования
 * на стороне клиента.
 */
@Data @AllArgsConstructor
public class ReviewDto {

    /** @brief Идентификатор отзыва. */
    private Integer id;

    /** @brief Идентификатор автора (используется клиентом для проверки владельца). */
    private Integer userId;

    /** @brief Имя автора для отображения. */
    private String userName;

    /** @brief Оценка от 1 до 5. */
    private Short rating;

    /** @brief Текст комментария. */
    private String comment;

    /** @brief Дата и время публикации отзыва. */
    private LocalDateTime createdAt;
}

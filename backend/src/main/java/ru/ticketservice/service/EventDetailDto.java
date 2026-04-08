package ru.ticketservice.service;

import lombok.*;
import java.util.List;

/**
 * @brief DTO детальной информации о мероприятии.
 *
 * Используется на странице мероприятия. Расширяет {@link EventDto}
 * полным описанием и списком отзывов посетителей.
 */
@Data @AllArgsConstructor
public class EventDetailDto {

    /** @brief Краткая информация о мероприятии (название, дата, цена и т.д.). */
    private EventDto event;

    /** @brief Полное описание мероприятия. */
    private String description;

    /** @brief Список отзывов, отсортированных по дате (новые первые). */
    private List<ReviewDto> reviews;
}

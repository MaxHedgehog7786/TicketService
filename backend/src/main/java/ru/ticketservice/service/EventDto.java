package ru.ticketservice.service;

import lombok.*;
import java.math.BigDecimal;

/**
 * @brief DTO краткой информации о мероприятии для списков и карточек.
 *
 * Используется на главной странице (афиша) и в списке избранного.
 * Содержит всё необходимое для отображения карточки мероприятия
 * без детального описания и списка отзывов.
 */
@Data @AllArgsConstructor
public class EventDto {

    /** @brief Идентификатор мероприятия. */
    private Integer id;

    /** @brief Название мероприятия. */
    private String title;

    /** @brief Название категории (например, «Концерт», «Спектакль»). */
    private String category;

    /** @brief Город проведения. */
    private String city;

    /** @brief Площадка проведения. */
    private String venue;

    /** @brief Дата и время в формате {@code dd.MM.yyyy HH:mm}. */
    private String date;

    /** @brief Минимальная цена билета. */
    private BigDecimal price;

    /** @brief URL изображения для афиши. */
    private String imageUrl;

    /** @brief Средняя оценка из отзывов (0.0 если отзывов нет). */
    private Double avgRating;
}

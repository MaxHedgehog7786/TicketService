package ru.ticketservice.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * @brief Справочник категорий мероприятий.
 *
 * Примеры: «Концерт», «Спектакль», «Фестиваль», «Выставка», «Спорт», «Семейное».
 * Соответствует таблице {@code categories}.
 */
@Entity @Table(name = "categories")
@Data @NoArgsConstructor @AllArgsConstructor
public class Category {

    /** @brief Уникальный идентификатор категории. */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** @brief Название категории (уникально, до 64 символов). */
    @Column(nullable = false, unique = true, length = 64)
    private String name;
}

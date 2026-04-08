package ru.ticketservice.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * @brief Справочник ролей пользователей.
 *
 * Поддерживаемые роли: {@code USER} (обычный покупатель) и {@code ADMIN} (администратор).
 * Соответствует таблице {@code roles}.
 */
@Entity @Table(name = "roles")
@Data @NoArgsConstructor @AllArgsConstructor
public class Role {

    /** @brief Уникальный идентификатор роли. */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * @brief Название роли (уникально, до 32 символов).
     * Возможные значения: {@code USER}, {@code ADMIN}.
     */
    @Column(nullable = false, unique = true, length = 32)
    private String name;
}

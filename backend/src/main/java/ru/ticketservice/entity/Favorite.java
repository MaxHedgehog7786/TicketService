package ru.ticketservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @brief Сущность «избранное» — связь пользователя с понравившимся мероприятием.
 *
 * Использует составной первичный ключ ({@code user_id}, {@code event_id}).
 * Соответствует таблице {@code favorites}.
 *
 * @see FavoriteId
 */
@Entity @Table(name = "favorites")
@Data @NoArgsConstructor @AllArgsConstructor
@IdClass(FavoriteId.class)
public class Favorite {

    /** @brief Идентификатор пользователя (часть составного PK). */
    @Id
    @Column(name = "user_id")
    private Integer userId;

    /** @brief Идентификатор мероприятия (часть составного PK). */
    @Id
    @Column(name = "event_id")
    private Integer eventId;

    /**
     * @brief Связанное мероприятие.
     * Загружается немедленно (EAGER) для отдачи списка избранных событий.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private Event event;

    /** @brief Дата и время добавления в избранное. */
    @Column(name = "added_at")
    private LocalDateTime addedAt = LocalDateTime.now();
}

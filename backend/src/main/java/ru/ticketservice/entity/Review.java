package ru.ticketservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @brief Сущность отзыва о мероприятии.
 *
 * Один пользователь может оставить не более одного отзыва на мероприятие
 * (ограничение уникальности по паре {@code user_id, event_id}).
 * Отзыв можно оставить только после посещения мероприятия.
 * Соответствует таблице {@code reviews}.
 */
@Entity
@Table(name = "reviews",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "event_id"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Review {

    /** @brief Уникальный идентификатор отзыва. */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** @brief Идентификатор автора отзыва. */
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    /**
     * @brief Автор отзыва.
     * Загружается лениво (LAZY). Исключён из JSON-сериализации.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnore @ToString.Exclude @EqualsAndHashCode.Exclude
    private User user;

    /** @brief Идентификатор мероприятия, на которое оставлен отзыв. */
    @Column(name = "event_id", nullable = false)
    private Integer eventId;

    /**
     * @brief Мероприятие, на которое оставлен отзыв.
     * Загружается лениво (LAZY). Исключён из JSON-сериализации.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    @JsonIgnore @ToString.Exclude @EqualsAndHashCode.Exclude
    private Event event;

    /** @brief Оценка от 1 до 5. */
    @Column(nullable = false)
    private Short rating;

    /** @brief Текст комментария к отзыву. */
    @Column(columnDefinition = "TEXT")
    private String comment;

    /** @brief Дата и время публикации отзыва. */
    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * @brief Признак верификации отзыва модератором.
     * {@code false} по умолчанию.
     */
    @Builder.Default
    @Column(name = "is_verified")
    private Boolean isVerified = false;
}

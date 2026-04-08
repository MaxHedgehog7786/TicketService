package ru.ticketservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @brief Сущность мероприятия.
 *
 * Содержит всю информацию о событии: название, описание, место проведения,
 * дату, категорию, базовую цену и изображение.
 * Соответствует таблице {@code events}.
 */
@Entity @Table(name = "events")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Event {

    /** @brief Уникальный идентификатор мероприятия. */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** @brief Название мероприятия (до 127 символов). */
    @Column(nullable = false, length = 127)
    private String title;

    /** @brief Подробное описание мероприятия. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** @brief Дата и время проведения мероприятия. */
    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    /** @brief Площадка проведения (название зала/стадиона). */
    @Column(nullable = false, length = 255)
    private String venue;

    /** @brief Город проведения мероприятия. */
    @Column(nullable = false, length = 64)
    private String city;

    /**
     * @brief Категория мероприятия (концерт, спектакль, фестиваль и т.д.).
     * Загружается немедленно (EAGER).
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    /** @brief Минимальная цена билета на мероприятие. */
    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    /** @brief URL изображения для афиши мероприятия. */
    @Column(name = "image_url")
    private String imageUrl;

    /** @brief Дата и время добавления мероприятия в систему. */
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * @brief Список мест в зале для данного мероприятия.
     * Исключён из сериализации JSON и сравнений во избежание циклических ссылок.
     */
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @JsonIgnore @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<Seat> seats;

    /**
     * @brief Список отзывов о мероприятии.
     * Исключён из сериализации JSON.
     */
    @OneToMany(mappedBy = "event")
    @JsonIgnore @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<Review> reviews;

    /**
     * @brief Возвращает дату и время в формате {@code dd.MM.yyyy HH:mm}.
     * @return отформатированная строка даты, либо пустая строка если {@code dateTime == null}
     */
    public String getDateTimeFormatted() {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
}

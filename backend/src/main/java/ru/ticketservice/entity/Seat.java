package ru.ticketservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @brief Сущность места в зале.
 *
 * Представляет конкретное место на мероприятии с информацией о ряде,
 * номере, секторе, цене и текущем статусе доступности.
 * Соответствует таблице {@code seats}.
 */
@Entity @Table(name = "seats")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Seat {

    /** @brief Уникальный идентификатор места. */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** @brief Идентификатор мероприятия, которому принадлежит место. */
    @Column(name = "event_id", nullable = false)
    private Integer eventId;

    /**
     * @brief Связанное мероприятие.
     * Загружается лениво (LAZY). Исключено из JSON и сравнений.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    @JsonIgnore @ToString.Exclude @EqualsAndHashCode.Exclude
    private Event event;

    /** @brief Номер места в ряду. */
    @Column(name = "seat_number", nullable = false)
    private Short seatNumber;

    /** @brief Номер ряда в зале. */
    @Column(name = "row_number", nullable = false)
    private Short rowNumber;

    /** @brief Название сектора/зоны (например, «Партер», «Балкон», «VIP»). */
    private String sector;

    /** @brief Цена билета для данного места. */
    @Column(nullable = false)
    private BigDecimal price;

    /**
     * @brief Текущий статус места.
     * По умолчанию — {@link SeatStatus#FREE}.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SeatStatus status = SeatStatus.FREE;

    /**
     * @brief Время до которого место зарезервировано.
     * {@code null} если место свободно или продано.
     */
    @Column(name = "reserved_until")
    private LocalDateTime reservedUntil;

    /**
     * @brief Возвращает человекочитаемое описание расположения места.
     * @return строка вида «Ряд N, Место M, Сектор X»
     */
    public String getVenueInfo() {
        return "Ряд " + rowNumber + ", Место " + seatNumber +
               (sector != null ? ", Сектор " + sector : "");
    }

    /**
     * @brief Возможные статусы места в зале.
     */
    public enum SeatStatus {
        /** Место свободно для бронирования. */
        FREE,
        /** Место временно зарезервировано (не более 10 минут). */
        RESERVED,
        /** Место продано, билет выдан. */
        SOLD
    }
}

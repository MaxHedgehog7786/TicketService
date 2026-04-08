package ru.ticketservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @brief Сущность электронного билета.
 *
 * Привязан к конкретному месту (@ref Seat) и мероприятию (@ref Event)
 * в рамках заказа (@ref Order). Содержит QR-код для верификации при входе.
 * Соответствует таблице {@code tickets}.
 */
@Entity @Table(name = "tickets")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket {

    /** @brief Уникальный идентификатор билета. */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * @brief Заказ, к которому относится билет.
     * Загружается лениво (LAZY).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private Order order;

    /** @brief Идентификатор мероприятия. */
    @Column(name = "event_id", nullable = false)
    private Integer eventId;

    /**
     * @brief Мероприятие билета.
     * Загружается немедленно (EAGER) для отображения в списке билетов.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private Event event;

    /** @brief Идентификатор места. */
    @Column(name = "seat_id", nullable = false)
    private Integer seatId;

    /**
     * @brief Место в зале.
     * Загружается немедленно (EAGER) для отображения деталей в PDF-билете.
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seat_id", insertable = false, updatable = false)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private Seat seat;

    /** @brief Цена билета на момент покупки. */
    @Column(nullable = false)
    private BigDecimal price;

    /**
     * @brief Текущий статус билета.
     * По умолчанию — {@link TicketStatus#PAID}.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TicketStatus status = TicketStatus.PAID;

    /** @brief UUID, закодированный в QR-код для верификации на входе. */
    @Column(name = "qr_code")
    private String qrCode;

    /** @brief Дата и время выдачи билета. */
    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * @brief Возможные статусы билета.
     */
    public enum TicketStatus {
        /** Билет оплачен и действителен. */
        PAID,
        /** Деньги за билет возвращены покупателю. */
        REFUNDED,
        /** Билет аннулирован. */
        CANCELLED
    }
}

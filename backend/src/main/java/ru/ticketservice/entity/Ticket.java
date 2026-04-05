package ru.ticketservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "tickets")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private Order order;

    @Column(name = "event_id", nullable = false)
    private Integer eventId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private Event event;

    @Column(name = "seat_id", nullable = false)
    private Integer seatId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seat_id", insertable = false, updatable = false)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private Seat seat;

    @Column(nullable = false)
    private BigDecimal price;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TicketStatus status = TicketStatus.PAID;

    @Column(name = "qr_code")
    private String qrCode;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TicketStatus { PAID, REFUNDED, CANCELLED }
}

package ru.ticketservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "seats")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Seat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "event_id", nullable = false)
    private Integer eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    @JsonIgnore @ToString.Exclude @EqualsAndHashCode.Exclude
    private Event event;

    @Column(name = "seat_number", nullable = false)
    private Short seatNumber;

    @Column(name = "row_number", nullable = false)
    private Short rowNumber;

    private String sector;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SeatStatus status = SeatStatus.FREE;

    @Column(name = "reserved_until")
    private LocalDateTime reservedUntil;

    public String getVenueInfo() {
        return "Ряд " + rowNumber + ", Место " + seatNumber +
               (sector != null ? ", Сектор " + sector : "");
    }

    public enum SeatStatus { FREE, RESERVED, SOLD }
}

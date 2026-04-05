package ru.ticketservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity @Table(name = "events")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Event {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 127)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false, length = 255)
    private String venue;

    @Column(nullable = false, length = 64)
    private String city;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @JsonIgnore @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<Seat> seats;

    @OneToMany(mappedBy = "event")
    @JsonIgnore @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<Review> reviews;

    public String getDateTimeFormatted() {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
}

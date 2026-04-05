package ru.ticketservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity @Table(name = "favorites")
@Data @NoArgsConstructor @AllArgsConstructor
@IdClass(FavoriteId.class)
public class Favorite {
    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Id
    @Column(name = "event_id")
    private Integer eventId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private Event event;

    @Column(name = "added_at")
    private LocalDateTime addedAt = LocalDateTime.now();
}

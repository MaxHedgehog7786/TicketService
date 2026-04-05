package ru.ticketservice.entity;

import lombok.*;

import java.io.Serializable;

@Data @NoArgsConstructor @AllArgsConstructor
public class FavoriteId implements Serializable {
    private Integer userId;
    private Integer eventId;
}

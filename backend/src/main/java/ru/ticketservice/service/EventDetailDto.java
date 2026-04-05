package ru.ticketservice.service;

import lombok.*;
import java.util.List;

@Data @AllArgsConstructor
public class EventDetailDto {
    private EventDto event;
    private String description;
    private List<ReviewDto> reviews;
}

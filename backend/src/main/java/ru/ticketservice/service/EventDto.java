package ru.ticketservice.service;

import lombok.*;
import java.math.BigDecimal;

@Data @AllArgsConstructor
public class EventDto {
    private Integer id;
    private String title;
    private String category;
    private String city;
    private String venue;
    private String date;
    private BigDecimal price;
    private String imageUrl;
    private Double avgRating;
}

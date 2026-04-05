package ru.ticketservice.service;

import lombok.*;
import java.time.LocalDateTime;

@Data @AllArgsConstructor
public class ReviewDto {
    private Integer id;
    private Integer userId;
    private String userName;
    private Short rating;
    private String comment;
    private LocalDateTime createdAt;
}

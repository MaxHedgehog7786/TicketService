package ru.ticketservice.service;

import lombok.*;

@Data
public class ReviewRequest {
    private Integer eventId;
    private Short rating;
    private String comment;
}

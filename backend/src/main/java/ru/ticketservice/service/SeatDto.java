package ru.ticketservice.service;

import lombok.*;
import java.math.BigDecimal;

@Data @AllArgsConstructor
public class SeatDto {
    private Integer id;
    private Short rowNumber;
    private Short seatNumber;
    private String sector;
    private BigDecimal price;
    private String status;
}

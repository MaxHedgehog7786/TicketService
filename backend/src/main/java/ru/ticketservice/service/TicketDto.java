package ru.ticketservice.service;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @AllArgsConstructor
public class TicketDto {
    private Integer id;
    private Integer eventId;
    private String eventTitle;
    private String eventDate;
    private Short rowNumber;
    private Short seatNumber;
    private String sector;
    private BigDecimal price;
    private String status;
    private String qrCode;
    private LocalDateTime createdAt;
}

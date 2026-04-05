package ru.ticketservice.service;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @AllArgsConstructor
public class ReserveResponse {
    private List<Integer> seatIds;
    private BigDecimal total;
    private LocalDateTime reservedUntil;
}

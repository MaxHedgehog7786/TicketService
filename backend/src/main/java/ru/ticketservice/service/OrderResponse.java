package ru.ticketservice.service;

import lombok.*;
import java.math.BigDecimal;

@Data @AllArgsConstructor
public class OrderResponse {
    private Integer orderId;
    private BigDecimal total;
    private int ticketCount;
}

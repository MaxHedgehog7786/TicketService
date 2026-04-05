package ru.ticketservice.service;

import lombok.*;
import java.util.List;

@Data
public class ConfirmRequest {
    private List<Integer> seatIds;
    private String paymentMethod;
    private String externalPaymentId;
}

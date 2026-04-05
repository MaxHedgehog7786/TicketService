package ru.ticketservice.service;

import lombok.*;
import java.util.List;

@Data
public class ReserveRequest {
    private List<Integer> seatIds;
}

package ru.ticketservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ticketservice.entity.*;
import ru.ticketservice.exception.ConflictException;
import ru.ticketservice.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final SeatRepository seatRepo;
    private final OrderRepository orderRepo;
    private final TicketRepository ticketRepo;
    private final TransactionRepository txRepo;

    public ReserveResponse reserve(Integer userId, List<Integer> seatIds) {
        List<Seat> seats = seatRepo.findFreeByIds(seatIds);
        if (seats.size() != seatIds.size())
            throw new ConflictException("Одно или несколько мест уже заняты");

        LocalDateTime until = LocalDateTime.now().plusMinutes(10);
        BigDecimal total = BigDecimal.ZERO;
        for (Seat s : seats) {
            s.setStatus(Seat.SeatStatus.RESERVED);
            s.setReservedUntil(until);
            total = total.add(s.getPrice());
        }
        seatRepo.saveAll(seats);
        return new ReserveResponse(seatIds, total, until);
    }

    public OrderResponse confirmPayment(Integer userId, List<Integer> seatIds,
                                        String paymentMethod, String externalPaymentId) {
        List<Seat> seats = seatRepo.findAllById(seatIds);
        for (Seat s : seats) {
            if (s.getStatus() != Seat.SeatStatus.RESERVED)
                throw new ConflictException("Место " + s.getId() + " не зарезервировано");
        }

        BigDecimal total = seats.stream()
            .map(Seat::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
            .userId(userId)
            .paymentStatus(true)
            .paymentMethod(paymentMethod)
            .totalAmount(total)
            .build();
        orderRepo.save(order);

        List<Ticket> tickets = new ArrayList<>();
        for (Seat seat : seats) {
            seat.setStatus(Seat.SeatStatus.SOLD);
            seat.setReservedUntil(null);
            Ticket ticket = Ticket.builder()
                .order(order)
                .eventId(seat.getEventId())
                .seatId(seat.getId())
                .price(seat.getPrice())
                .qrCode(UUID.randomUUID().toString())
                .build();
            tickets.add(ticket);
        }
        seatRepo.saveAll(seats);
        ticketRepo.saveAll(tickets);

        Transaction tx = Transaction.builder()
            .orderId(order.getId())
            .amount(total)
            .status(Transaction.TxStatus.SUCCESS)
            .paymentMethod(paymentMethod)
            .externalId(externalPaymentId)
            .build();
        txRepo.save(tx);

        return new OrderResponse(order.getId(), total, tickets.size());
    }

    @Transactional(readOnly = true)
    public List<TicketDto> getUserTickets(Integer userId) {
        return orderRepo.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .flatMap(o -> ticketRepo.findByOrderId(o.getId()).stream())
            .map(this::toTicketDto)
            .toList();
    }

    @Scheduled(fixedRate = 300_000)
    public void releaseExpiredReservations() {
        List<Seat> expired = seatRepo.findExpiredReservations(LocalDateTime.now());
        expired.forEach(s -> { s.setStatus(Seat.SeatStatus.FREE); s.setReservedUntil(null); });
        seatRepo.saveAll(expired);
    }

    private TicketDto toTicketDto(Ticket t) {
        return new TicketDto(t.getId(), t.getEventId(),
            t.getEvent().getTitle(), t.getEvent().getDateTimeFormatted(),
            t.getSeat().getRowNumber(), t.getSeat().getSeatNumber(),
            t.getSeat().getSector(), t.getPrice(),
            t.getStatus().name(), t.getQrCode(), t.getCreatedAt());
    }
}

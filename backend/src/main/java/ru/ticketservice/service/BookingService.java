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

/**
 * @brief Сервис бронирования и оформления билетов.
 *
 * Реализует двухэтапный процесс покупки:
 * <ol>
 *   <li>Временное бронирование мест (10 минут) — {@link #reserve}</li>
 *   <li>Подтверждение оплаты и выдача билетов — {@link #confirmPayment}</li>
 * </ol>
 * Просроченные бронирования автоматически освобождаются каждые 5 минут.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final SeatRepository seatRepo;
    private final OrderRepository orderRepo;
    private final TicketRepository ticketRepo;
    private final TransactionRepository txRepo;

    /**
     * @brief Временно резервирует выбранные места.
     *
     * Резервирование действует 10 минут. Если хотя бы одно из
     * запрошенных мест уже занято — выбрасывается исключение.
     *
     * @param userId  идентификатор пользователя
     * @param seatIds список идентификаторов мест
     * @return {@link ReserveResponse} со списком мест, суммой и временем истечения брони
     * @throws ConflictException если одно или несколько мест уже недоступны
     */
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

    /**
     * @brief Подтверждает оплату и оформляет билеты.
     *
     * Создаёт {@link Order}, генерирует {@link Ticket} для каждого места
     * (со случайным UUID в качестве QR-кода) и записывает {@link Transaction}.
     * Места переводятся в статус {@link Seat.SeatStatus#SOLD}.
     *
     * @param userId            идентификатор покупателя
     * @param seatIds           список идентификаторов зарезервированных мест
     * @param paymentMethod     способ оплаты (например, «CARD»)
     * @param externalPaymentId идентификатор транзакции в платёжной системе
     * @return {@link OrderResponse} с номером заказа, суммой и количеством билетов
     * @throws ConflictException если место не находится в статусе RESERVED
     */
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

    /**
     * @brief Возвращает все билеты пользователя, отсортированные по дате покупки (новые первые).
     *
     * @param userId идентификатор пользователя
     * @return список {@link TicketDto} со всеми билетами пользователя
     */
    @Transactional(readOnly = true)
    public List<TicketDto> getUserTickets(Integer userId) {
        return orderRepo.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .flatMap(o -> ticketRepo.findByOrderId(o.getId()).stream())
            .map(this::toTicketDto)
            .toList();
    }

    /**
     * @brief Планировщик: освобождает просроченные резервирования.
     *
     * Запускается каждые 5 минут. Переводит места с истёкшим временем
     * бронирования обратно в статус {@link Seat.SeatStatus#FREE}.
     */
    @Scheduled(fixedRate = 300_000)
    public void releaseExpiredReservations() {
        List<Seat> expired = seatRepo.findExpiredReservations(LocalDateTime.now());
        expired.forEach(s -> { s.setStatus(Seat.SeatStatus.FREE); s.setReservedUntil(null); });
        seatRepo.saveAll(expired);
    }

    /**
     * @brief Преобразует сущность {@link Ticket} в DTO для передачи клиенту.
     * @param t билет
     * @return {@link TicketDto} с данными о месте, мероприятии и статусе
     */
    private TicketDto toTicketDto(Ticket t) {
        return new TicketDto(t.getId(), t.getEventId(),
            t.getEvent().getTitle(), t.getEvent().getDateTimeFormatted(),
            t.getSeat().getRowNumber(), t.getSeat().getSeatNumber(),
            t.getSeat().getSector(), t.getPrice(),
            t.getStatus().name(), t.getQrCode(), t.getCreatedAt());
    }
}

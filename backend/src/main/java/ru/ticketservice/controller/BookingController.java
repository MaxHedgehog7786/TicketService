package ru.ticketservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.ticketservice.service.*;

import java.util.List;

/**
 * @brief REST-контроллер бронирования и оформления билетов.
 *
 * Управляет двухэтапным процессом покупки билетов и предоставляет
 * доступ к личным билетам пользователя. Все эндпоинты требуют аутентификации.
 */
@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final PdfService pdfService;
    private final UserService userService;

    /**
     * @brief Резервирует выбранные места (шаг 1 покупки).
     *
     * {@code POST /booking/reserve}
     *
     * Резервирование удерживает места 10 минут, после чего они
     * автоматически освобождаются.
     *
     * @param req список идентификаторов мест для бронирования
     * @param ud  аутентифицированный пользователь
     * @return {@code 200 OK} с {@link ReserveResponse} (сумма, время истечения брони)
     */
    @PostMapping("/booking/reserve")
    public ResponseEntity<ReserveResponse> reserve(@RequestBody ReserveRequest req,
                                                   @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(bookingService.reserve(userService.resolveId(ud), req.getSeatIds()));
    }

    /**
     * @brief Подтверждает оплату и оформляет билеты (шаг 2 покупки).
     *
     * {@code POST /booking/confirm}
     *
     * @param req данные оплаты (список мест, способ оплаты, внешний ID транзакции)
     * @param ud  аутентифицированный пользователь
     * @return {@code 200 OK} с {@link OrderResponse} (номер заказа, сумма, кол-во билетов)
     */
    @PostMapping("/booking/confirm")
    public ResponseEntity<OrderResponse> confirm(@RequestBody ConfirmRequest req,
                                                  @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(bookingService.confirmPayment(userService.resolveId(ud),
                req.getSeatIds(), req.getPaymentMethod(), req.getExternalPaymentId()));
    }

    /**
     * @brief Возвращает все билеты текущего пользователя.
     *
     * {@code GET /tickets}
     *
     * @param ud аутентифицированный пользователь
     * @return {@code 200 OK} со списком {@link TicketDto}
     */
    @GetMapping("/tickets")
    public ResponseEntity<List<TicketDto>> myTickets(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(bookingService.getUserTickets(userService.resolveId(ud)));
    }

    /**
     * @brief Генерирует и скачивает PDF-файл билета.
     *
     * {@code GET /tickets/{id}/pdf}
     *
     * Проверяет принадлежность билета текущему пользователю.
     *
     * @param id идентификатор билета
     * @param ud аутентифицированный пользователь
     * @return {@code 200 OK} с PDF-документом (Content-Type: application/pdf)
     */
    @GetMapping("/tickets/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Integer id,
                                              @AuthenticationPrincipal UserDetails ud) {
        byte[] pdf = pdfService.generateTicketPdf(id, userService.resolveId(ud));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ticket-" + id + ".pdf")
                .body(pdf);
    }
}

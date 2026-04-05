package ru.ticketservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.ticketservice.service.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final PdfService pdfService;
    private final UserService userService;

    @PostMapping("/booking/reserve")
    public ResponseEntity<ReserveResponse> reserve(@RequestBody ReserveRequest req,
                                                   @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(bookingService.reserve(userService.resolveId(ud), req.getSeatIds()));
    }

    @PostMapping("/booking/confirm")
    public ResponseEntity<OrderResponse> confirm(@RequestBody ConfirmRequest req,
                                                  @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(bookingService.confirmPayment(userService.resolveId(ud),
                req.getSeatIds(), req.getPaymentMethod(), req.getExternalPaymentId()));
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<TicketDto>> myTickets(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(bookingService.getUserTickets(userService.resolveId(ud)));
    }

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

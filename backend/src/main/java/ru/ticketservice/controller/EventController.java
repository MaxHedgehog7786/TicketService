package ru.ticketservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ticketservice.service.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Page<EventDto>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(eventService.search(q, city,
                (category != null && !category.isBlank()) ? category : null,
                from, to, PageRequest.of(page, size)));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<EventDto>> upcoming(@RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(eventService.getUpcoming(limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDetailDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(eventService.getDetail(id));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<List<SeatDto>> getSeats(@PathVariable Integer id) {
        return ResponseEntity.ok(eventService.getSeats(id));
    }
}

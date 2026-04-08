package ru.ticketservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ticketservice.service.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @brief REST-контроллер мероприятий.
 *
 * Предоставляет публичные (без аутентификации) эндпоинты для поиска
 * мероприятий, получения детальной информации и списка мест в зале.
 * Базовый путь: {@code /events}.
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * @brief Возвращает страницу мероприятий с фильтрацией.
     *
     * {@code GET /events}
     *
     * @param q        текстовый поиск по названию и описанию (необязательно)
     * @param city     фильтр по городу (необязательно)
     * @param category фильтр по названию категории (необязательно)
     * @param from     нижняя граница даты в формате ISO-8601 (необязательно)
     * @param to       верхняя граница даты в формате ISO-8601 (необязательно)
     * @param page     номер страницы (по умолчанию {@code 0})
     * @param size     размер страницы (по умолчанию {@code 12})
     * @return {@code 200 OK} со страницей {@link EventDto}
     */
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

    /**
     * @brief Возвращает список ближайших предстоящих мероприятий.
     *
     * {@code GET /events/upcoming}
     *
     * @param limit максимальное количество результатов (по умолчанию {@code 6})
     * @return {@code 200 OK} со списком {@link EventDto}
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<EventDto>> upcoming(@RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(eventService.getUpcoming(limit));
    }

    /**
     * @brief Возвращает полную информацию о мероприятии.
     *
     * {@code GET /events/{id}}
     *
     * @param id идентификатор мероприятия
     * @return {@code 200 OK} с {@link EventDetailDto} (данные + список отзывов)
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventDetailDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(eventService.getDetail(id));
    }

    /**
     * @brief Возвращает список мест в зале для мероприятия.
     *
     * {@code GET /events/{id}/seats}
     *
     * @param id идентификатор мероприятия
     * @return {@code 200 OK} со списком {@link SeatDto}
     */
    @GetMapping("/{id}/seats")
    public ResponseEntity<List<SeatDto>> getSeats(@PathVariable Integer id) {
        return ResponseEntity.ok(eventService.getSeats(id));
    }
}

package ru.ticketservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.ticketservice.service.*;

import java.util.List;

/**
 * @brief REST-контроллер избранных мероприятий.
 *
 * Позволяет аутентифицированным пользователям управлять списком
 * избранных мероприятий. Базовый путь: {@code /favorites}.
 */
@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final EventService eventService;
    private final UserService userService;

    /**
     * @brief Возвращает список избранных мероприятий текущего пользователя.
     *
     * {@code GET /favorites}
     *
     * @param ud аутентифицированный пользователь
     * @return {@code 200 OK} со списком {@link EventDto}
     */
    @GetMapping
    public ResponseEntity<List<EventDto>> list(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(eventService.getFavorites(userService.resolveId(ud)));
    }

    /**
     * @brief Добавляет мероприятие в избранное.
     *
     * {@code POST /favorites/{eventId}}
     *
     * Идемпотентная операция: повторный запрос не вызывает ошибки.
     *
     * @param eventId идентификатор мероприятия
     * @param ud      аутентифицированный пользователь
     * @return {@code 200 OK}
     */
    @PostMapping("/{eventId}")
    public ResponseEntity<Void> add(@PathVariable Integer eventId,
                                    @AuthenticationPrincipal UserDetails ud) {
        eventService.addFavorite(userService.resolveId(ud), eventId);
        return ResponseEntity.ok().build();
    }

    /**
     * @brief Удаляет мероприятие из избранного.
     *
     * {@code DELETE /favorites/{eventId}}
     *
     * @param eventId идентификатор мероприятия
     * @param ud      аутентифицированный пользователь
     * @return {@code 204 No Content}
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> remove(@PathVariable Integer eventId,
                                       @AuthenticationPrincipal UserDetails ud) {
        eventService.removeFavorite(userService.resolveId(ud), eventId);
        return ResponseEntity.noContent().build();
    }
}

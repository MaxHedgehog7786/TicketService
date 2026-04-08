package ru.ticketservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.ticketservice.service.*;

import java.util.List;

/**
 * @brief REST-контроллер отзывов о мероприятиях.
 *
 * Чтение отзывов доступно без аутентификации.
 * Создание, редактирование и удаление требуют JWT-токена.
 * Базовый путь: {@code /reviews}.
 */
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    /**
     * @brief Возвращает список отзывов на мероприятие.
     *
     * {@code GET /reviews/{eventId}}
     *
     * @param eventId идентификатор мероприятия
     * @return {@code 200 OK} со списком {@link ReviewDto} (новые первые)
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<List<ReviewDto>> list(@PathVariable Integer eventId) {
        return ResponseEntity.ok(reviewService.getByEvent(eventId));
    }

    /**
     * @brief Добавляет новый отзыв на мероприятие.
     *
     * {@code POST /reviews}
     *
     * Требует наличия купленного билета на мероприятие.
     *
     * @param req данные отзыва (eventId, rating, comment)
     * @param ud  аутентифицированный пользователь
     * @return {@code 201 Created} с созданным {@link ReviewDto}
     */
    @PostMapping
    public ResponseEntity<ReviewDto> add(@RequestBody ReviewRequest req,
                                         @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.addReview(userService.resolveId(ud),
                        req.getEventId(), req.getRating(), req.getComment()));
    }

    /**
     * @brief Обновляет оценку и текст отзыва.
     *
     * {@code PUT /reviews/{id}}
     *
     * Доступно только автору отзыва.
     *
     * @param id  идентификатор редактируемого отзыва
     * @param req новые данные (rating, comment)
     * @param ud  аутентифицированный пользователь
     * @return {@code 200 OK} с обновлённым {@link ReviewDto}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> update(@PathVariable Integer id,
                                             @RequestBody ReviewRequest req,
                                             @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(reviewService.updateReview(id, userService.resolveId(ud),
                req.getRating(), req.getComment()));
    }

    /**
     * @brief Удаляет отзыв.
     *
     * {@code DELETE /reviews/{id}}
     *
     * Доступно только автору отзыва.
     *
     * @param id идентификатор удаляемого отзыва
     * @param ud аутентифицированный пользователь
     * @return {@code 204 No Content}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id,
                                        @AuthenticationPrincipal UserDetails ud) {
        reviewService.deleteReview(id, userService.resolveId(ud));
        return ResponseEntity.noContent().build();
    }
}

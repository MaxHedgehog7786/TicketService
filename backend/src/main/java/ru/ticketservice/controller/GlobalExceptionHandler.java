package ru.ticketservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ticketservice.exception.*;

/**
 * @brief Глобальный обработчик исключений REST API.
 *
 * Перехватывает бизнес-исключения, брошенные в сервисном слое,
 * и преобразует их в соответствующие HTTP-ответы с JSON-телом.
 *
 * Таблица сопоставлений:
 * | Исключение             | HTTP-статус |
 * |------------------------|-------------|
 * | NotFoundException      | 404         |
 * | ConflictException      | 409         |
 * | UnauthorizedException  | 401         |
 * | ForbiddenException     | 403         |
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @brief Обрабатывает ситуацию, когда запрошенный ресурс не найден.
     * @param e исключение с сообщением об ошибке
     * @return {@code 404 Not Found} с телом {@link ErrorResponse}
     */
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(NotFoundException e) {
        return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage()));
    }

    /**
     * @brief Обрабатывает конфликт данных (например, дублирование записи).
     * @param e исключение с сообщением об ошибке
     * @return {@code 409 Conflict} с телом {@link ErrorResponse}
     */
    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ErrorResponse> conflict(ConflictException e) {
        return ResponseEntity.status(409).body(new ErrorResponse(e.getMessage()));
    }

    /**
     * @brief Обрабатывает ошибку аутентификации (неверный логин/пароль).
     * @param e исключение с сообщением об ошибке
     * @return {@code 401 Unauthorized} с телом {@link ErrorResponse}
     */
    @ExceptionHandler(UnauthorizedException.class)
    ResponseEntity<ErrorResponse> unauthorized(UnauthorizedException e) {
        return ResponseEntity.status(401).body(new ErrorResponse(e.getMessage()));
    }

    /**
     * @brief Обрабатывает ошибку доступа (операция недоступна для текущего пользователя).
     * @param e исключение с сообщением об ошибке
     * @return {@code 403 Forbidden} с телом {@link ErrorResponse}
     */
    @ExceptionHandler(ForbiddenException.class)
    ResponseEntity<ErrorResponse> forbidden(ForbiddenException e) {
        return ResponseEntity.status(403).body(new ErrorResponse(e.getMessage()));
    }

    /**
     * @brief DTO для тела ответа с описанием ошибки.
     * @param error человекочитаемое сообщение об ошибке
     */
    record ErrorResponse(String error) {}
}

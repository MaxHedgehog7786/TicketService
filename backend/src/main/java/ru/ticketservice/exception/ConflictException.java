package ru.ticketservice.exception;

/**
 * @brief Исключение конфликта данных (HTTP 409).
 *
 * Выбрасывается при попытке создать дублирующую запись
 * (например, регистрация с существующим email или повторный отзыв).
 */
public class ConflictException extends RuntimeException {

    /**
     * @brief Создаёт исключение с описанием конфликта.
     * @param message описание причины конфликта
     */
    public ConflictException(String message) { super(message); }
}

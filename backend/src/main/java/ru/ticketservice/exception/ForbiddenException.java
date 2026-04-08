package ru.ticketservice.exception;

/**
 * @brief Исключение запрета доступа (HTTP 403).
 *
 * Выбрасывается когда аутентифицированный пользователь пытается
 * выполнить операцию, на которую у него нет прав
 * (например, редактировать чужой отзыв).
 */
public class ForbiddenException extends RuntimeException {

    /**
     * @brief Создаёт исключение с описанием причины запрета.
     * @param message описание причины отказа в доступе
     */
    public ForbiddenException(String message) { super(message); }
}

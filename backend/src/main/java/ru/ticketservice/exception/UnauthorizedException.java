package ru.ticketservice.exception;

/**
 * @brief Исключение ошибки аутентификации (HTTP 401).
 *
 * Выбрасывается при попытке входа с неверными учётными данными
 * (неверный логин или пароль).
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * @brief Создаёт исключение с описанием причины ошибки аутентификации.
     * @param message описание причины отказа
     */
    public UnauthorizedException(String message) { super(message); }
}

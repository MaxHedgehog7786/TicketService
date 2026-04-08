package ru.ticketservice.exception;

/**
 * @brief Исключение «ресурс не найден» (HTTP 404).
 *
 * Выбрасывается когда запрошенный объект отсутствует в базе данных
 * (например, мероприятие, билет или отзыв с указанным идентификатором).
 */
public class NotFoundException extends RuntimeException {

    /**
     * @brief Создаёт исключение с описанием отсутствующего ресурса.
     * @param message описание того, что не было найдено
     */
    public NotFoundException(String message) { super(message); }
}

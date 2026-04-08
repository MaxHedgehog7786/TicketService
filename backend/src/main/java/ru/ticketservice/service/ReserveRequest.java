package ru.ticketservice.service;

import lombok.*;
import java.util.List;

/**
 * @brief DTO запроса на временное бронирование мест.
 *
 * Используется эндпоинтом {@code POST /booking/reserve}.
 */
@Data
public class ReserveRequest {

    /** @brief Список идентификаторов мест для бронирования. */
    private List<Integer> seatIds;
}

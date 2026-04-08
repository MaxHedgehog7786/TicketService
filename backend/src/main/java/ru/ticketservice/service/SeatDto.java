package ru.ticketservice.service;

import lombok.*;
import java.math.BigDecimal;

/**
 * @brief DTO места в зале для отображения схемы посадки.
 *
 * Передаётся клиенту при загрузке схемы зала и используется
 * компонентом SeatMap для отрисовки каждого места с его
 * текущим статусом и ценой.
 */
@Data @AllArgsConstructor
public class SeatDto {

    /** @brief Идентификатор места (используется для бронирования). */
    private Integer id;

    /** @brief Номер ряда в зале. */
    private Short rowNumber;

    /** @brief Номер места в ряду. */
    private Short seatNumber;

    /** @brief Название сектора/зоны (например, «Партер», «VIP»). */
    private String sector;

    /** @brief Цена билета для данного места. */
    private BigDecimal price;

    /**
     * @brief Текущий статус места.
     * Возможные значения: {@code FREE}, {@code RESERVED}, {@code SOLD}.
     */
    private String status;
}

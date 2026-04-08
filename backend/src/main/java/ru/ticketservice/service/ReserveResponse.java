package ru.ticketservice.service;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @brief DTO ответа на запрос временного бронирования.
 *
 * Возвращается после успешного резервирования мест.
 * Клиент использует {@code reservedUntil} для отображения таймера оплаты.
 */
@Data @AllArgsConstructor
public class ReserveResponse {

    /** @brief Список идентификаторов успешно зарезервированных мест. */
    private List<Integer> seatIds;

    /** @brief Итоговая сумма к оплате. */
    private BigDecimal total;

    /** @brief Время истечения резервирования (через 10 минут после брони). */
    private LocalDateTime reservedUntil;
}

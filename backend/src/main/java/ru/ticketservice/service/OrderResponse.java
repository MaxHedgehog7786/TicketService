package ru.ticketservice.service;

import lombok.*;
import java.math.BigDecimal;

/**
 * @brief DTO ответа на успешное оформление заказа.
 *
 * Возвращается после подтверждения оплаты.
 * Клиент использует данные для отображения сообщения об успешной покупке.
 */
@Data @AllArgsConstructor
public class OrderResponse {

    /** @brief Идентификатор созданного заказа. */
    private Integer orderId;

    /** @brief Итоговая сумма оплаченного заказа. */
    private BigDecimal total;

    /** @brief Количество оформленных билетов. */
    private int ticketCount;
}

package ru.ticketservice.service;

import lombok.*;
import java.util.List;

/**
 * @brief DTO запроса на подтверждение оплаты.
 *
 * Используется эндпоинтом {@code POST /booking/confirm}.
 * Передаётся после успешной обработки платежа на стороне клиента.
 */
@Data
public class ConfirmRequest {

    /** @brief Список идентификаторов зарезервированных мест для оформления. */
    private List<Integer> seatIds;

    /** @brief Способ оплаты (например, {@code CARD}, {@code SBP}). */
    private String paymentMethod;

    /**
     * @brief Идентификатор транзакции во внешней платёжной системе.
     * Сохраняется в {@link ru.ticketservice.entity.Transaction} для сверки.
     */
    private String externalPaymentId;
}

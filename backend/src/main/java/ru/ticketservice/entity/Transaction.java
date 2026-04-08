package ru.ticketservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @brief Сущность финансовой транзакции.
 *
 * Фиксирует каждую попытку оплаты заказа с её статусом и внешним
 * идентификатором платёжной системы. Соответствует таблице {@code transactions}.
 */
@Entity @Table(name = "transactions")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {

    /** @brief Уникальный идентификатор транзакции. */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** @brief Идентификатор заказа, к которому относится транзакция. */
    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    /** @brief Сумма транзакции. */
    @Column(nullable = false)
    private BigDecimal amount;

    /**
     * @brief Статус обработки транзакции.
     * По умолчанию — {@link TxStatus#PENDING}.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TxStatus status = TxStatus.PENDING;

    /** @brief Способ оплаты (например, «CARD», «SBP»). */
    @Column(name = "payment_method", length = 32)
    private String paymentMethod;

    /** @brief Идентификатор транзакции во внешней платёжной системе. */
    @Column(name = "external_id", length = 128)
    private String externalId;

    /** @brief Дата и время создания транзакции. */
    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * @brief Возможные статусы транзакции.
     */
    public enum TxStatus {
        /** Транзакция ожидает обработки. */
        PENDING,
        /** Платёж успешно проведён. */
        SUCCESS,
        /** Платёж отклонён. */
        FAILED,
        /** Выполнен возврат средств. */
        REFUNDED
    }
}

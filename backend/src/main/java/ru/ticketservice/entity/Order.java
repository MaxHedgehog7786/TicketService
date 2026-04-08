package ru.ticketservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @brief Сущность заказа.
 *
 * Объединяет один или несколько билетов, купленных пользователем за одну транзакцию.
 * Соответствует таблице {@code orders}.
 */
@Entity @Table(name = "orders")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {

    /** @brief Уникальный идентификатор заказа. */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** @brief Идентификатор пользователя, оформившего заказ. */
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    /** @brief Дата и время создания заказа. */
    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * @brief Статус оплаты заказа.
     * {@code true} — оплачен, {@code false} — не оплачен.
     */
    @Builder.Default
    @Column(name = "payment_status")
    private Boolean paymentStatus = false;

    /** @brief Способ оплаты (например, «CARD»). */
    @Column(name = "payment_method", length = 32)
    private String paymentMethod;

    /** @brief Итоговая сумма заказа. */
    @Builder.Default
    @Column(name = "total_amount")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /**
     * @brief Список билетов, входящих в заказ.
     * Каскадно удаляются вместе с заказом.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<Ticket> tickets;
}

package ru.ticketservice.service;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @brief DTO электронного билета для личного кабинета.
 *
 * Содержит все данные, необходимые для отображения билета:
 * информацию о мероприятии, месте в зале, статусе и QR-коде.
 */
@Data @AllArgsConstructor
public class TicketDto {

    /** @brief Идентификатор билета. */
    private Integer id;

    /** @brief Идентификатор мероприятия. */
    private Integer eventId;

    /** @brief Название мероприятия (денормализовано для отображения в списке). */
    private String eventTitle;

    /** @brief Дата мероприятия в формате {@code dd.MM.yyyy HH:mm}. */
    private String eventDate;

    /** @brief Номер ряда в зале. */
    private Short rowNumber;

    /** @brief Номер места в ряду. */
    private Short seatNumber;

    /** @brief Название сектора/зоны. */
    private String sector;

    /** @brief Цена билета на момент покупки. */
    private BigDecimal price;

    /** @brief Текущий статус билета: {@code PAID}, {@code REFUNDED} или {@code CANCELLED}. */
    private String status;

    /** @brief UUID для QR-кода, предъявляемого на входе. */
    private String qrCode;

    /** @brief Дата и время выдачи билета. */
    private LocalDateTime createdAt;
}

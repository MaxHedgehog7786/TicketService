package ru.ticketservice.entity;

import lombok.*;

import java.io.Serializable;

/**
 * @brief Составной первичный ключ для сущности {@link Favorite}.
 *
 * Требуется JPA для корректной работы аннотации {@code @IdClass}.
 * Должен реализовывать {@link Serializable} и иметь правильные
 * {@code equals}/{@code hashCode}.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class FavoriteId implements Serializable {

    /** @brief Идентификатор пользователя. */
    private Integer userId;

    /** @brief Идентификатор мероприятия. */
    private Integer eventId;
}

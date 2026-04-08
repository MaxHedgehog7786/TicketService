package ru.ticketservice.repository;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ticketservice.entity.Event;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @brief Репозиторий мероприятий с поддержкой полнотекстового поиска и фильтрации.
 *
 * Использует нативный SQL вместо JPQL для корректной обработки {@code null}-параметров
 * в PostgreSQL (Hibernate 6 передаёт null как нетипизированный {@code bytea},
 * что вызывает ошибку {@code operator does not exist: lower(bytea)}).
 * Решение: явное приведение типов через {@code CAST(:param AS text)}.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

    /**
     * @brief Ищет мероприятия с многокритериальной фильтрацией.
     *
     * Все параметры опциональны — {@code null} означает «без фильтра».
     * Поиск по тексту выполняется по полям {@code title} и {@code description}
     * без учёта регистра. Фильтрация по категории — по полю {@code c.name}
     * через JOIN с таблицей {@code categories}.
     *
     * @param q        текстовый запрос (необязательно)
     * @param city     город (необязательно)
     * @param category название категории (необязательно)
     * @param from     нижняя граница даты (необязательно)
     * @param to       верхняя граница даты (необязательно)
     * @param pageable параметры пагинации и сортировки
     * @return страница мероприятий, отсортированных по дате (по возрастанию)
     */
    @Query(value = """
        SELECT e.* FROM events e
        JOIN categories c ON e.category_id = c.id
        WHERE (CAST(:q AS text) IS NULL
               OR lower(e.title) LIKE lower('%' || CAST(:q AS text) || '%')
               OR lower(e.description) LIKE lower('%' || CAST(:q AS text) || '%'))
          AND (CAST(:city AS text) IS NULL OR e.city = CAST(:city AS text))
          AND (CAST(:category AS text) IS NULL OR lower(c.name) = lower(CAST(:category AS text)))
          AND (CAST(:from AS timestamp) IS NULL OR e.date_time >= CAST(:from AS timestamp))
          AND (CAST(:to AS timestamp) IS NULL OR e.date_time <= CAST(:to AS timestamp))
        ORDER BY e.date_time ASC
        """,
        countQuery = """
        SELECT count(*) FROM events e
        JOIN categories c ON e.category_id = c.id
        WHERE (CAST(:q AS text) IS NULL
               OR lower(e.title) LIKE lower('%' || CAST(:q AS text) || '%')
               OR lower(e.description) LIKE lower('%' || CAST(:q AS text) || '%'))
          AND (CAST(:city AS text) IS NULL OR e.city = CAST(:city AS text))
          AND (CAST(:category AS text) IS NULL OR lower(c.name) = lower(CAST(:category AS text)))
          AND (CAST(:from AS timestamp) IS NULL OR e.date_time >= CAST(:from AS timestamp))
          AND (CAST(:to AS timestamp) IS NULL OR e.date_time <= CAST(:to AS timestamp))
        """,
        nativeQuery = true)
    Page<Event> search(@Param("q") String q,
                       @Param("city") String city,
                       @Param("category") String category,
                       @Param("from") LocalDateTime from,
                       @Param("to") LocalDateTime to,
                       Pageable pageable);

    /**
     * @brief Возвращает список предстоящих мероприятий.
     *
     * @param now      текущее время (отсекает прошедшие мероприятия)
     * @param pageable ограничение на количество результатов
     * @return список мероприятий в хронологическом порядке
     */
    @Query("SELECT e FROM Event e WHERE e.dateTime > :now ORDER BY e.dateTime ASC")
    List<Event> findUpcoming(@Param("now") LocalDateTime now, Pageable pageable);
}

package ru.ticketservice.repository;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ticketservice.entity.Event;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

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

    @Query("SELECT e FROM Event e WHERE e.dateTime > :now ORDER BY e.dateTime ASC")
    List<Event> findUpcoming(@Param("now") LocalDateTime now, Pageable pageable);
}

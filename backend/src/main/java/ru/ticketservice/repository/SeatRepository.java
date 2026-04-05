package ru.ticketservice.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ticketservice.entity.Seat;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {
    List<Seat> findByEventIdOrderByRowNumberAscSeatNumberAsc(Integer eventId);

    @Query("SELECT s FROM Seat s WHERE s.id IN :ids AND s.status = 'FREE'")
    List<Seat> findFreeByIds(@Param("ids") List<Integer> ids);

    @Query("SELECT s FROM Seat s WHERE s.status = 'RESERVED' AND s.reservedUntil < :now")
    List<Seat> findExpiredReservations(@Param("now") LocalDateTime now);
}

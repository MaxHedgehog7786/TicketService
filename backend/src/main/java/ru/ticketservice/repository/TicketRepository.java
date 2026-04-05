package ru.ticketservice.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ticketservice.entity.Ticket;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByOrderId(Integer orderId);

    boolean existsByOrderUserIdAndEventId(Integer userId, Integer eventId);

    @Query("SELECT t FROM Ticket t WHERE t.id = :id AND t.order.userId = :userId")
    Optional<Ticket> findByIdAndUserId(@Param("id") Integer id, @Param("userId") Integer userId);
}

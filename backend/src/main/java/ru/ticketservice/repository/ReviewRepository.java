package ru.ticketservice.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ticketservice.entity.Review;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByEventIdOrderByCreatedAtDesc(Integer eventId);
    boolean existsByUserIdAndEventId(Integer userId, Integer eventId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.eventId = :eventId")
    Double avgRatingByEventId(@Param("eventId") Integer eventId);
}

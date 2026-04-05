package ru.ticketservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ticketservice.entity.Favorite;
import ru.ticketservice.entity.FavoriteId;

import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {
    List<Favorite> findByUserId(Integer userId);
    boolean existsByUserIdAndEventId(Integer userId, Integer eventId);
    void deleteByUserIdAndEventId(Integer userId, Integer eventId);
}

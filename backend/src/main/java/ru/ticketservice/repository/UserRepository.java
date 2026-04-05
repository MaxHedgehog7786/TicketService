package ru.ticketservice.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import ru.ticketservice.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByLogin(String login);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByLogin(String login);

    @Query("SELECT u FROM User u WHERE u.subscribed = true")
    List<User> findSubscribedUsers();
}

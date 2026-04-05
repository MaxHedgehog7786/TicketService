package ru.ticketservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.ticketservice.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;

    public Integer resolveId(UserDetails ud) {
        return userRepo.findByLogin(ud.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found: " + ud.getUsername()))
            .getId();
    }
}

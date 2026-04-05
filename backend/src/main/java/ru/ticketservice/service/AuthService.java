package ru.ticketservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ticketservice.entity.*;
import ru.ticketservice.exception.*;
import ru.ticketservice.repository.*;
import ru.ticketservice.security.JwtService;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final JwtService jwtService;

    private String sha256(String raw) {
        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes());
            var sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail()))
            throw new ConflictException("Email уже зарегистрирован");
        if (userRepo.existsByLogin(req.getLogin()))
            throw new ConflictException("Логин уже занят");

        Role role = roleRepo.findByName("USER").orElseThrow();
        User user = User.builder()
            .login(req.getLogin())
            .password(sha256(req.getPassword()))
            .name(req.getName())
            .surname(req.getSurname())
            .email(req.getEmail())
            .phone(req.getPhone())
            .role(role)
            .subscribed(req.getSubscribed() != null ? req.getSubscribed() : false)
            .build();

        userRepo.save(user);
        String token = generateToken(user);
        return new AuthResponse(user.getId(), token, user.getLogin(), user.getName(), role.getName());
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepo.findByLogin(req.getLogin())
            .orElseThrow(() -> new UnauthorizedException("Неверный логин или пароль"));

        if (!user.getPassword().equals(sha256(req.getPassword())))
            throw new UnauthorizedException("Неверный логин или пароль");

        String token = generateToken(user);
        return new AuthResponse(user.getId(), token, user.getLogin(), user.getName(), user.getRole().getName());
    }

    public User updateProfile(Integer userId, UpdateProfileRequest req) {
        User user = userRepo.findById(userId).orElseThrow();
        if (req.getName() != null) user.setName(req.getName());
        if (req.getSurname() != null) user.setSurname(req.getSurname());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getSubscribed() != null) user.setSubscribed(req.getSubscribed());
        return userRepo.save(user);
    }

    private String generateToken(User user) {
        return jwtService.generateToken(
            org.springframework.security.core.userdetails.User.builder()
                .username(user.getLogin())
                .password(user.getPassword())
                .roles(user.getRole().getName())
                .build()
        );
    }
}

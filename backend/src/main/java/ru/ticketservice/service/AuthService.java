package ru.ticketservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ticketservice.entity.*;
import ru.ticketservice.exception.*;
import ru.ticketservice.repository.*;
import ru.ticketservice.security.JwtService;

/**
 * @brief Сервис аутентификации и управления профилем.
 *
 * Обеспечивает регистрацию новых пользователей, вход по логину/паролю
 * и обновление личных данных. Пароли хранятся в виде SHA-256 хешей.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final JwtService jwtService;

    /**
     * @brief Вычисляет SHA-256 хеш строки.
     * @param raw исходная строка (например, пароль пользователя)
     * @return шестнадцатеричный SHA-256 хеш
     */
    private String sha256(String raw) {
        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes());
            var sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    /**
     * @brief Регистрирует нового пользователя и возвращает JWT-токен.
     *
     * Проверяет уникальность email и логина перед созданием записи.
     * Новому пользователю автоматически присваивается роль {@code USER}.
     *
     * @param req данные для регистрации
     * @return {@link AuthResponse} с токеном и информацией о пользователе
     * @throws ConflictException если email или логин уже зарегистрированы
     */
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

    /**
     * @brief Аутентифицирует пользователя по логину и паролю.
     *
     * @param req логин и пароль
     * @return {@link AuthResponse} с JWT-токеном и данными пользователя
     * @throws UnauthorizedException если логин не найден или пароль неверен
     */
    public AuthResponse login(LoginRequest req) {
        User user = userRepo.findByLogin(req.getLogin())
            .orElseThrow(() -> new UnauthorizedException("Неверный логин или пароль"));

        if (!user.getPassword().equals(sha256(req.getPassword())))
            throw new UnauthorizedException("Неверный логин или пароль");

        String token = generateToken(user);
        return new AuthResponse(user.getId(), token, user.getLogin(), user.getName(), user.getRole().getName());
    }

    /**
     * @brief Обновляет личные данные пользователя.
     *
     * Обновляются только переданные (не null) поля: имя, фамилия, телефон, подписка.
     *
     * @param userId идентификатор пользователя
     * @param req    новые данные профиля
     * @return обновлённый объект {@link User}
     */
    public User updateProfile(Integer userId, UpdateProfileRequest req) {
        User user = userRepo.findById(userId).orElseThrow();
        if (req.getName() != null) user.setName(req.getName());
        if (req.getSurname() != null) user.setSurname(req.getSurname());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getSubscribed() != null) user.setSubscribed(req.getSubscribed());
        return userRepo.save(user);
    }

    /**
     * @brief Генерирует JWT-токен для пользователя.
     * @param user объект пользователя
     * @return подписанный JWT-токен
     */
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

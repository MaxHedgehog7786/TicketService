package ru.ticketservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ru.ticketservice.service.*;

/**
 * @brief REST-контроллер аутентификации.
 *
 * Предоставляет публичные (не требующие токена) эндпоинты для
 * регистрации и входа в систему. Базовый путь: {@code /auth}.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * @brief Регистрирует нового пользователя.
     *
     * {@code POST /auth/register}
     *
     * @param req данные новой учётной записи (логин, пароль, имя, email и т.д.)
     * @return {@code 201 Created} с {@link AuthResponse} (id, token, login, name, role)
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    /**
     * @brief Аутентифицирует пользователя по логину и паролю.
     *
     * {@code POST /auth/login}
     *
     * @param req логин и пароль
     * @return {@code 200 OK} с {@link AuthResponse} (id, token, login, name, role)
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}

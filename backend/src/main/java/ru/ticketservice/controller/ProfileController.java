package ru.ticketservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.ticketservice.service.*;

/**
 * @brief REST-контроллер личного кабинета пользователя.
 *
 * Предоставляет эндпоинты для обновления профиля и управления подпиской
 * на еженедельную рассылку. Базовый путь: {@code /profile}.
 * Все эндпоинты требуют аутентификации.
 */
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * @brief Обновляет данные профиля текущего пользователя.
     *
     * {@code PUT /profile}
     *
     * Обновляются только переданные (не null) поля: имя, фамилия, телефон, подписка.
     *
     * @param req новые данные профиля
     * @param ud  аутентифицированный пользователь
     * @return {@code 200 OK} с обновлённым объектом {@link ru.ticketservice.entity.User}
     */
    @PutMapping
    public ResponseEntity<?> update(@RequestBody UpdateProfileRequest req,
                                    @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(authService.updateProfile(userService.resolveId(ud), req));
    }

    /**
     * @brief Включает подписку на еженедельную рассылку рекомендаций.
     *
     * {@code POST /profile/subscribe}
     *
     * @param ud аутентифицированный пользователь
     * @return {@code 200 OK}
     */
    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@AuthenticationPrincipal UserDetails ud) {
        authService.updateProfile(userService.resolveId(ud),
                new UpdateProfileRequest(null, null, null, true));
        return ResponseEntity.ok().build();
    }
}

package ru.ticketservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.ticketservice.repository.UserRepository;

/**
 * @brief Вспомогательный сервис для работы с пользователями.
 *
 * Предоставляет утилитные методы, используемые контроллерами для
 * преобразования объектов Spring Security в идентификаторы пользователей.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;

    /**
     * @brief Получает идентификатор пользователя из объекта Spring Security.
     *
     * Выполняет поиск пользователя в базе данных по логину из {@link UserDetails}.
     *
     * @param ud объект деталей пользователя из контекста безопасности
     * @return идентификатор пользователя {@code (Integer)}
     * @throws RuntimeException если пользователь с указанным логином не найден
     */
    public Integer resolveId(UserDetails ud) {
        return userRepo.findByLogin(ud.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found: " + ud.getUsername()))
            .getId();
    }
}

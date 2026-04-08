package ru.ticketservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @brief Сущность пользователя системы.
 *
 * Хранит учётные данные, личную информацию и настройки подписки.
 * Соответствует таблице {@code users} в базе данных.
 */
@Entity @Table(name = "users")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    /** @brief Уникальный идентификатор пользователя. */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** @brief Уникальный логин для входа в систему (до 64 символов). */
    @Column(nullable = false, unique = true, length = 64)
    private String login;

    /** @brief Пароль, хранится в виде SHA-256 хеша. */
    @Column(nullable = false, length = 64)
    private String password;

    /** @brief Имя пользователя. */
    @Column(nullable = false, length = 64)
    private String name;

    /** @brief Фамилия пользователя. */
    @Column(nullable = false, length = 64)
    private String surname;

    /** @brief Уникальный адрес электронной почты (до 128 символов). */
    @Column(nullable = false, unique = true, length = 128)
    private String email;

    /** @brief Номер телефона (необязательно, до 16 символов). */
    @Column(length = 16)
    private String phone;

    /**
     * @brief Роль пользователя (USER или ADMIN).
     * Загружается немедленно (EAGER) для корректной работы Spring Security.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    /** @brief Флаг подписки на еженедельную рассылку рекомендаций. */
    @Builder.Default
    @Column(nullable = false)
    private Boolean subscribed = false;

    /** @brief Дата и время регистрации пользователя. */
    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}

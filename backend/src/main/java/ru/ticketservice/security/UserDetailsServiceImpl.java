package ru.ticketservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import ru.ticketservice.entity.User;
import ru.ticketservice.repository.UserRepository;

/**
 * @brief Реализация {@link UserDetailsService} для Spring Security.
 *
 * Загружает данные пользователя из базы данных по логину
 * и конвертирует сущность {@link User} в объект {@link UserDetails},
 * понятный Spring Security.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * @brief Загружает пользователя по логину.
     *
     * @param username логин пользователя
     * @return объект {@link UserDetails} с логином, хешем пароля и ролями
     * @throws UsernameNotFoundException если пользователь с данным логином не найден
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getLogin())
            .password(user.getPassword())
            .roles(user.getRole().getName())
            .build();
    }
}

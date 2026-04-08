package ru.ticketservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import ru.ticketservice.security.JwtAuthFilter;
import ru.ticketservice.security.UserDetailsServiceImpl;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

/**
 * @brief Конфигурация Spring Security.
 *
 * Настраивает:
 * <ul>
 *   <li>Stateless-сессии (JWT вместо cookie-сессий)</li>
 *   <li>Правила авторизации запросов</li>
 *   <li>CORS (разрешённые origins из конфигурации)</li>
 *   <li>JWT-фильтр аутентификации</li>
 * </ul>
 *
 * Публичные эндпоинты (без токена):
 * <ul>
 *   <li>{@code POST /auth/**} — регистрация и вход</li>
 *   <li>{@code GET /events}, {@code GET /events/**} — просмотр афиши</li>
 *   <li>{@code GET /reviews/**} — чтение отзывов</li>
 * </ul>
 * Раздел {@code /admin/**} требует роли {@code ADMIN}.
 * Все остальные запросы требуют аутентификации.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    /** @brief Список разрешённых CORS origins из конфигурации (через запятую). */
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * @brief Создаёт цепочку фильтров безопасности.
     *
     * @param http объект конфигурации HTTP безопасности
     * @return настроенная {@link SecurityFilterChain}
     * @throws Exception при ошибке конфигурации
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/events", "/events/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/reviews", "/reviews/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .userDetailsService(userDetailsService)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    /**
     * @brief Создаёт бин кодировщика паролей BCrypt.
     *
     * Используется Spring Security при сравнении паролей.
     * В данном проекте пароли хранятся как SHA-256, поэтому бин
     * зарегистрирован для совместимости с инфраструктурой, но
     * хеширование выполняется вручную в {@link ru.ticketservice.service.AuthService}.
     *
     * @return {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * @brief Создаёт источник CORS-конфигурации.
     *
     * Разрешает все методы (GET, POST, PUT, DELETE, OPTIONS) и заголовки
     * для origins, перечисленных в {@code app.cors.allowed-origins}.
     *
     * @return настроенный {@link CorsConfigurationSource}
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

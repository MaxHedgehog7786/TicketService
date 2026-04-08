package ru.ticketservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

/**
 * @brief Сервис генерации и валидации JWT-токенов.
 *
 * Использует алгоритм HMAC-SHA256. Секрет и срок действия токена
 * задаются через конфигурацию приложения:
 * <ul>
 *   <li>{@code jwt.secret} — ключ подписи (минимум 32 байта)</li>
 *   <li>{@code jwt.expiration} — время жизни токена в миллисекундах</li>
 * </ul>
 */
@Service
public class JwtService {

    /** @brief Секретный ключ подписи токена из конфигурации. */
    @Value("${jwt.secret}")
    private String secret;

    /** @brief Время жизни токена в миллисекундах. */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * @brief Создаёт ключ подписи из секрета.
     * @return объект {@link Key} для подписи/верификации JWT
     */
    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * @brief Генерирует JWT-токен для пользователя.
     *
     * Токен содержит логин пользователя (subject), дату выдачи и дату истечения.
     *
     * @param userDetails данные пользователя из Spring Security
     * @return подписанный JWT-токен в компактном формате
     */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * @brief Извлекает логин пользователя из токена.
     *
     * @param token JWT-токен
     * @return логин пользователя (subject claim)
     */
    public String extractUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(getKey()).build()
            .parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * @brief Проверяет валидность токена.
     *
     * Токен считается валидным, если его subject совпадает с логином
     * пользователя и срок действия не истёк.
     *
     * @param token       JWT-токен
     * @param userDetails данные пользователя для сравнения
     * @return {@code true} если токен валиден
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isExpired(token);
    }

    /**
     * @brief Проверяет, истёк ли срок действия токена.
     * @param token JWT-токен
     * @return {@code true} если токен просрочен
     */
    private boolean isExpired(String token) {
        Date exp = Jwts.parserBuilder().setSigningKey(getKey()).build()
            .parseClaimsJws(token).getBody().getExpiration();
        return exp.before(new Date());
    }
}

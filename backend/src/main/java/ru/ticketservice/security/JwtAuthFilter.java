package ru.ticketservice.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @brief Фильтр аутентификации по JWT-токену.
 *
 * Выполняется один раз для каждого HTTP-запроса ({@link OncePerRequestFilter}).
 * Извлекает JWT-токен из заголовка {@code Authorization: Bearer <token>},
 * верифицирует его и устанавливает аутентификацию в {@link SecurityContextHolder}.
 *
 * Если заголовок отсутствует или токен невалиден — запрос передаётся дальше
 * по цепочке фильтров без установки аутентификации.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * @brief Обрабатывает входящий HTTP-запрос.
     *
     * Алгоритм:
     * <ol>
     *   <li>Читает заголовок {@code Authorization}</li>
     *   <li>Если заголовок начинается с {@code Bearer } — извлекает токен</li>
     *   <li>Загружает пользователя и проверяет валидность токена</li>
     *   <li>При успехе устанавливает {@link UsernamePasswordAuthenticationToken} в контекст</li>
     * </ol>
     *
     * @param req   входящий HTTP-запрос
     * @param res   HTTP-ответ
     * @param chain цепочка фильтров
     * @throws ServletException при ошибке фильтрации
     * @throws IOException      при ошибке ввода-вывода
     */
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(token, userDetails)) {
                var authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        chain.doFilter(req, res);
    }
}

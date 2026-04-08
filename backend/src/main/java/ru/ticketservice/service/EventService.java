package ru.ticketservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ticketservice.entity.*;
import ru.ticketservice.exception.NotFoundException;
import ru.ticketservice.repository.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @brief Сервис работы с мероприятиями, избранным и местами в зале.
 *
 * Предоставляет поиск с фильтрацией, получение детальной информации
 * о мероприятии, управление избранным и преобразование сущностей в DTO.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepo;
    private final SeatRepository seatRepo;
    private final FavoriteRepository favoriteRepo;
    private final ReviewRepository reviewRepo;

    /**
     * @brief Ищет мероприятия по заданным фильтрам с постраничной разбивкой.
     *
     * Пустые строки в параметрах интерпретируются как «без фильтра» ({@code null}).
     *
     * @param q        поисковый запрос по названию/описанию (необязательно)
     * @param city     фильтр по городу (необязательно)
     * @param category фильтр по названию категории (необязательно)
     * @param from     нижняя граница даты (необязательно)
     * @param to       верхняя граница даты (необязательно)
     * @param pageable параметры постраничной разбивки и сортировки
     * @return страница {@link EventDto}
     */
    public Page<EventDto> search(String q, String city, String category,
                                  LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return eventRepo.search(
            (q != null && q.isBlank()) ? null : q,
            (city != null && city.isBlank()) ? null : city,
            category, from, to, pageable
        ).map(this::toDto);
    }

    /**
     * @brief Возвращает ближайшие предстоящие мероприятия.
     *
     * @param limit максимальное количество результатов
     * @return список {@link EventDto}, отсортированный по дате
     */
    public List<EventDto> getUpcoming(int limit) {
        return eventRepo.findUpcoming(LocalDateTime.now(), PageRequest.of(0, limit))
            .stream().map(this::toDto).toList();
    }

    /**
     * @brief Возвращает полную информацию о мероприятии, включая отзывы.
     *
     * @param id идентификатор мероприятия
     * @return {@link EventDetailDto} с данными события и списком отзывов
     * @throws NotFoundException если мероприятие не найдено
     */
    public EventDetailDto getDetail(Integer id) {
        Event e = eventRepo.findById(id)
            .orElseThrow(() -> new NotFoundException("Мероприятие не найдено"));
        Double avg = reviewRepo.avgRatingByEventId(id);
        List<Review> reviews = reviewRepo.findByEventIdOrderByCreatedAtDesc(id);
        return toDetailDto(e, avg, reviews);
    }

    /**
     * @brief Возвращает список мест в зале для мероприятия.
     *
     * @param eventId идентификатор мероприятия
     * @return список {@link SeatDto}, отсортированный по ряду и номеру места
     */
    public List<SeatDto> getSeats(Integer eventId) {
        return seatRepo.findByEventIdOrderByRowNumberAscSeatNumberAsc(eventId)
            .stream().map(this::toSeatDto).toList();
    }

    /**
     * @brief Добавляет мероприятие в избранное пользователя.
     *
     * Идемпотентная операция: повторное добавление игнорируется.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор мероприятия
     */
    @Transactional
    public void addFavorite(Integer userId, Integer eventId) {
        if (!favoriteRepo.existsByUserIdAndEventId(userId, eventId)) {
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setEventId(eventId);
            favoriteRepo.save(fav);
        }
    }

    /**
     * @brief Удаляет мероприятие из избранного пользователя.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор мероприятия
     */
    @Transactional
    public void removeFavorite(Integer userId, Integer eventId) {
        favoriteRepo.deleteByUserIdAndEventId(userId, eventId);
    }

    /**
     * @brief Возвращает список избранных мероприятий пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список {@link EventDto}
     */
    public List<EventDto> getFavorites(Integer userId) {
        return favoriteRepo.findByUserId(userId).stream()
            .map(f -> toDto(f.getEvent())).toList();
    }

    /**
     * @brief Преобразует сущность {@link Event} в краткое DTO.
     *
     * Дополнительно вычисляет среднюю оценку из таблицы отзывов.
     *
     * @param e сущность мероприятия
     * @return {@link EventDto}
     */
    public EventDto toDto(Event e) {
        Double avg = reviewRepo.avgRatingByEventId(e.getId());
        return new EventDto(e.getId(), e.getTitle(), e.getCategory().getName(),
            e.getCity(), e.getVenue(), e.getDateTimeFormatted(),
            e.getBasePrice(), e.getImageUrl(),
            avg != null ? avg : 0.0);
    }

    /**
     * @brief Преобразует сущность {@link Event} и связанные отзывы в детальное DTO.
     * @param e       мероприятие
     * @param avg     средняя оценка (может быть {@code null})
     * @param reviews список отзывов
     * @return {@link EventDetailDto}
     */
    private EventDetailDto toDetailDto(Event e, Double avg, List<Review> reviews) {
        return new EventDetailDto(toDto(e), e.getDescription(), reviews.stream()
            .map(r -> new ReviewDto(r.getId(), r.getUserId(), r.getUser().getName(),
                r.getRating(), r.getComment(), r.getCreatedAt())).toList());
    }

    /**
     * @brief Преобразует сущность {@link Seat} в DTO.
     * @param s место в зале
     * @return {@link SeatDto}
     */
    private SeatDto toSeatDto(Seat s) {
        return new SeatDto(s.getId(), s.getRowNumber(), s.getSeatNumber(),
            s.getSector(), s.getPrice(), s.getStatus().name());
    }
}

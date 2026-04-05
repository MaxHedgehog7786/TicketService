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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepo;
    private final SeatRepository seatRepo;
    private final FavoriteRepository favoriteRepo;
    private final ReviewRepository reviewRepo;

    public Page<EventDto> search(String q, String city, String category,
                                  LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return eventRepo.search(
            (q != null && q.isBlank()) ? null : q,
            (city != null && city.isBlank()) ? null : city,
            category, from, to, pageable
        ).map(this::toDto);
    }

    public List<EventDto> getUpcoming(int limit) {
        return eventRepo.findUpcoming(LocalDateTime.now(), PageRequest.of(0, limit))
            .stream().map(this::toDto).toList();
    }

    public EventDetailDto getDetail(Integer id) {
        Event e = eventRepo.findById(id)
            .orElseThrow(() -> new NotFoundException("Мероприятие не найдено"));
        Double avg = reviewRepo.avgRatingByEventId(id);
        List<Review> reviews = reviewRepo.findByEventIdOrderByCreatedAtDesc(id);
        return toDetailDto(e, avg, reviews);
    }

    public List<SeatDto> getSeats(Integer eventId) {
        return seatRepo.findByEventIdOrderByRowNumberAscSeatNumberAsc(eventId)
            .stream().map(this::toSeatDto).toList();
    }

    @Transactional
    public void addFavorite(Integer userId, Integer eventId) {
        if (!favoriteRepo.existsByUserIdAndEventId(userId, eventId)) {
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setEventId(eventId);
            favoriteRepo.save(fav);
        }
    }

    @Transactional
    public void removeFavorite(Integer userId, Integer eventId) {
        favoriteRepo.deleteByUserIdAndEventId(userId, eventId);
    }

    public List<EventDto> getFavorites(Integer userId) {
        return favoriteRepo.findByUserId(userId).stream()
            .map(f -> toDto(f.getEvent())).toList();
    }

    public EventDto toDto(Event e) {
        Double avg = reviewRepo.avgRatingByEventId(e.getId());
        return new EventDto(e.getId(), e.getTitle(), e.getCategory().getName(),
            e.getCity(), e.getVenue(), e.getDateTimeFormatted(),
            e.getBasePrice(), e.getImageUrl(),
            avg != null ? avg : 0.0);
    }

    private EventDetailDto toDetailDto(Event e, Double avg, List<Review> reviews) {
        return new EventDetailDto(toDto(e), e.getDescription(), reviews.stream()
            .map(r -> new ReviewDto(r.getId(), r.getUserId(), r.getUser().getName(),
                r.getRating(), r.getComment(), r.getCreatedAt())).toList());
    }

    private SeatDto toSeatDto(Seat s) {
        return new SeatDto(s.getId(), s.getRowNumber(), s.getSeatNumber(),
            s.getSector(), s.getPrice(), s.getStatus().name());
    }
}

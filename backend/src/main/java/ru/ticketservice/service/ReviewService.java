package ru.ticketservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ticketservice.entity.Review;
import ru.ticketservice.entity.User;
import ru.ticketservice.exception.*;
import ru.ticketservice.repository.*;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final TicketRepository ticketRepo;
    private final UserRepository userRepo;

    public ReviewDto addReview(Integer userId, Integer eventId, short rating, String comment) {
        boolean attended = ticketRepo.existsByOrderUserIdAndEventId(userId, eventId);
        if (!attended)
            throw new ForbiddenException("Отзыв можно оставить только после посещения мероприятия");
        if (reviewRepo.existsByUserIdAndEventId(userId, eventId))
            throw new ConflictException("Вы уже оставляли отзыв на это мероприятие");

        Review review = Review.builder()
            .userId(userId).eventId(eventId).rating(rating).comment(comment).build();
        Review saved = reviewRepo.save(review);
        String name = userRepo.findById(userId).map(User::getName).orElse("Пользователь");
        return toDto(saved, name);
    }

    public ReviewDto updateReview(Integer reviewId, Integer userId, short rating, String comment) {
        Review review = reviewRepo.findById(reviewId)
            .orElseThrow(() -> new NotFoundException("Отзыв не найден"));
        if (!review.getUserId().equals(userId))
            throw new ForbiddenException("Нельзя редактировать чужой отзыв");
        review.setRating(rating);
        review.setComment(comment);
        Review saved = reviewRepo.save(review);
        String name = userRepo.findById(userId).map(User::getName).orElse("Пользователь");
        return toDto(saved, name);
    }

    public void deleteReview(Integer reviewId, Integer userId) {
        Review review = reviewRepo.findById(reviewId)
            .orElseThrow(() -> new NotFoundException("Отзыв не найден"));
        if (!review.getUserId().equals(userId))
            throw new ForbiddenException("Нельзя удалить чужой отзыв");
        reviewRepo.delete(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getByEvent(Integer eventId) {
        return reviewRepo.findByEventIdOrderByCreatedAtDesc(eventId).stream()
            .map(r -> toDto(r, r.getUser().getName()))
            .toList();
    }

    private ReviewDto toDto(Review r, String userName) {
        return new ReviewDto(r.getId(), r.getUserId(), userName,
            r.getRating(), r.getComment(), r.getCreatedAt());
    }
}

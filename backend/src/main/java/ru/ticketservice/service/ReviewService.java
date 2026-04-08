package ru.ticketservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ticketservice.entity.Review;
import ru.ticketservice.entity.User;
import ru.ticketservice.exception.*;
import ru.ticketservice.repository.*;

import java.util.List;

/**
 * @brief Сервис управления отзывами о мероприятиях.
 *
 * Позволяет добавлять, редактировать и удалять отзывы.
 * Добавление возможно только после посещения мероприятия (наличие билета).
 * Редактирование и удаление доступны только автору отзыва.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final TicketRepository ticketRepo;
    private final UserRepository userRepo;

    /**
     * @brief Добавляет новый отзыв о мероприятии.
     *
     * @param userId   идентификатор автора
     * @param eventId  идентификатор мероприятия
     * @param rating   оценка от 1 до 5
     * @param comment  текст комментария
     * @return созданный {@link ReviewDto}
     * @throws ForbiddenException  если у пользователя нет билета на мероприятие
     * @throws ConflictException   если пользователь уже оставлял отзыв на это мероприятие
     */
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

    /**
     * @brief Обновляет оценку и текст существующего отзыва.
     *
     * @param reviewId идентификатор отзыва
     * @param userId   идентификатор пользователя (проверка владельца)
     * @param rating   новая оценка от 1 до 5
     * @param comment  новый текст комментария
     * @return обновлённый {@link ReviewDto}
     * @throws NotFoundException  если отзыв не найден
     * @throws ForbiddenException если пользователь не является автором отзыва
     */
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

    /**
     * @brief Удаляет отзыв.
     *
     * @param reviewId идентификатор отзыва
     * @param userId   идентификатор пользователя (проверка владельца)
     * @throws NotFoundException  если отзыв не найден
     * @throws ForbiddenException если пользователь не является автором отзыва
     */
    public void deleteReview(Integer reviewId, Integer userId) {
        Review review = reviewRepo.findById(reviewId)
            .orElseThrow(() -> new NotFoundException("Отзыв не найден"));
        if (!review.getUserId().equals(userId))
            throw new ForbiddenException("Нельзя удалить чужой отзыв");
        reviewRepo.delete(review);
    }

    /**
     * @brief Возвращает все отзывы на мероприятие, отсортированные по дате (новые первые).
     *
     * @param eventId идентификатор мероприятия
     * @return список {@link ReviewDto}
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getByEvent(Integer eventId) {
        return reviewRepo.findByEventIdOrderByCreatedAtDesc(eventId).stream()
            .map(r -> toDto(r, r.getUser().getName()))
            .toList();
    }

    /**
     * @brief Преобразует сущность {@link Review} в DTO.
     * @param r        отзыв
     * @param userName имя автора
     * @return {@link ReviewDto}
     */
    private ReviewDto toDto(Review r, String userName) {
        return new ReviewDto(r.getId(), r.getUserId(), userName,
            r.getRating(), r.getComment(), r.getCreatedAt());
    }
}

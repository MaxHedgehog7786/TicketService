package ru.ticketservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.ticketservice.entity.User;
import ru.ticketservice.repository.UserRepository;

import java.util.List;

/**
 * @brief Сервис отправки электронных писем.
 *
 * Обеспечивает:
 * <ul>
 *   <li>Еженедельную рассылку рекомендаций подписчикам (каждый понедельник в 09:00)</li>
 *   <li>Отправку подтверждения покупки билетов</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepo;
    private final EventService eventService;

    /**
     * @brief Еженедельная рассылка рекомендаций подписчикам.
     *
     * Запускается автоматически каждый понедельник в 09:00 (cron).
     * Отправляет письмо с 5 ближайшими мероприятиями всем пользователям
     * с активной подпиской ({@code subscribed = true}).
     */
    @Scheduled(cron = "0 0 9 * * MON")
    public void sendWeeklyRecommendations() {
        List<User> subscribers = userRepo.findSubscribedUsers();
        List<EventDto> upcoming = eventService.getUpcoming(5);
        String body = buildEmailBody(upcoming);
        for (User user : subscribers) {
            sendEmail(user.getEmail(), "TicketService — рекомендации на неделю", body);
        }
    }

    /**
     * @brief Отправляет пользователю подтверждение успешной покупки билетов.
     *
     * @param user  пользователь-покупатель
     * @param order данные оформленного заказа
     */
    public void sendBookingConfirmation(User user, OrderResponse order) {
        sendEmail(user.getEmail(),
            "Подтверждение покупки билетов — TicketService",
            "Ваш заказ #" + order.getOrderId() + " успешно оформлен. " +
            "Билеты доступны в личном кабинете.");
    }

    /**
     * @brief Отправляет простое текстовое письмо.
     *
     * @param to      адрес получателя
     * @param subject тема письма
     * @param text    текст письма
     */
    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("noreply@ticketservice.ru");
        mailSender.send(message);
    }

    /**
     * @brief Формирует текст письма с рекомендациями мероприятий.
     *
     * @param events список мероприятий для включения в письмо
     * @return готовый текст письма
     */
    private String buildEmailBody(List<EventDto> events) {
        StringBuilder sb = new StringBuilder("Ближайшие мероприятия для вас:\n\n");
        events.forEach(e -> sb.append("• ").append(e.getTitle())
            .append(" — ").append(e.getDate())
            .append(" (от ").append(e.getPrice()).append(" \u20BD)\n"));
        sb.append("\nПосетите ticketservice.ru для покупки билетов.");
        return sb.toString();
    }
}

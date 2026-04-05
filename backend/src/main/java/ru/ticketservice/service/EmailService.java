package ru.ticketservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.ticketservice.entity.User;
import ru.ticketservice.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepo;
    private final EventService eventService;

    @Scheduled(cron = "0 0 9 * * MON")
    public void sendWeeklyRecommendations() {
        List<User> subscribers = userRepo.findSubscribedUsers();
        List<EventDto> upcoming = eventService.getUpcoming(5);
        String body = buildEmailBody(upcoming);
        for (User user : subscribers) {
            sendEmail(user.getEmail(), "TicketService — рекомендации на неделю", body);
        }
    }

    public void sendBookingConfirmation(User user, OrderResponse order) {
        sendEmail(user.getEmail(),
            "Подтверждение покупки билетов — TicketService",
            "Ваш заказ #" + order.getOrderId() + " успешно оформлен. " +
            "Билеты доступны в личном кабинете.");
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("noreply@ticketservice.ru");
        mailSender.send(message);
    }

    private String buildEmailBody(List<EventDto> events) {
        StringBuilder sb = new StringBuilder("Ближайшие мероприятия для вас:\n\n");
        events.forEach(e -> sb.append("• ").append(e.getTitle())
            .append(" — ").append(e.getDate())
            .append(" (от ").append(e.getPrice()).append(" \u20BD)\n"));
        sb.append("\nПосетите ticketservice.ru для покупки билетов.");
        return sb.toString();
    }
}

package ru.ticketservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @brief Точка входа приложения TicketService.
 *
 * Запускает встроенный сервер Spring Boot и включает поддержку
 * планировщика задач (@ref EmailService и @ref BookingService).
 */
@SpringBootApplication
@EnableScheduling
public class TicketServiceApplication {

    /**
     * @brief Запуск приложения.
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(TicketServiceApplication.class, args);
    }
}

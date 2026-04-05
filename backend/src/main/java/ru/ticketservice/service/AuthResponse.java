package ru.ticketservice.service;

import lombok.*;

@Data @AllArgsConstructor
public class AuthResponse {
    Integer id;
    String token;
    String login;
    String name;
    String role;
}

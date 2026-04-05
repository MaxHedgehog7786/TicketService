package ru.ticketservice.service;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank String login;
    @NotBlank String password;
}

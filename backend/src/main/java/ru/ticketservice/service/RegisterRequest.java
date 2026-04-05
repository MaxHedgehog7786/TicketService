package ru.ticketservice.service;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
public class RegisterRequest {
    @NotBlank String login;
    @NotBlank @Size(min = 8) String password;
    @NotBlank String name;
    @NotBlank String surname;
    @NotBlank @Email String email;
    String phone;
    Boolean subscribed = false;
}

package ru.ticketservice.service;

import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor
public class UpdateProfileRequest {
    String name;
    String surname;
    String phone;
    Boolean subscribed;
}

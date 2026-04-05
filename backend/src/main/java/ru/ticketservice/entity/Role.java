package ru.ticketservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "roles")
@Data @NoArgsConstructor @AllArgsConstructor
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 32)
    private String name; // USER | ADMIN
}

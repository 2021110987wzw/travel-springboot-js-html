// User.java
package com.example.travel.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(length = 255)
    private String avatarUrl;

    @Column(length = 10)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    public enum Gender {
        MALE,
        FEMALE,
        OTHER
    }
}

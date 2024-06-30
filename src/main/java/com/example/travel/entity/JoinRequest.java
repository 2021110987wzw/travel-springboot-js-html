package com.example.travel.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class JoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false)
    private boolean approved = false;

    @Column(nullable = false)
    private boolean processed = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Transient
    private String userDisplayName; // 临时存储用户名

    @Transient
    private String teamDisplayName; // 临时存储队伍名
}

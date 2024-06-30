package com.example.travel.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String destination;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Column(nullable = false)
    private int maxMembers;

    @Column(nullable = false)
    private int currentMembers = 0;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false, length = 255)
    private String teamName;

    @Column(columnDefinition = "TEXT")
    private String teamDescription;

    @Column(nullable = false)
    private boolean isClosed = false;

    public int getCurrentMembers() {
        return currentMembers;
    }

    public void setCurrentMembers(int currentMembers) {
        this.currentMembers = currentMembers;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }
}

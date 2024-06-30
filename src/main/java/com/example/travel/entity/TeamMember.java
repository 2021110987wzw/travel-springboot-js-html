package com.example.travel.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Column(nullable = false, length = 50)
    private String role = "member";

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Column(name = "team_id", insertable = false, updatable = false)
    private Long teamId;

    public Long getTeamId() {
        return team != null ? team.getId() : null;
    }

    public void setTeamId(Long teamId) {
        if (this.team == null) {
            this.team = new Team();
        }
        this.team.setId(teamId);
    }

    public void setUserId(Long userId) {
        if (this.user == null) {
            this.user = new User();
        }
        this.user.setId(userId);
    }
}

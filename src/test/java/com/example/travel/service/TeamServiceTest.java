package com.example.travel.service;

import com.example.travel.entity.Team;
import com.example.travel.entity.User;
import com.example.travel.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class TeamServiceTest {

    @InjectMocks
    private TeamService teamService;

    @Mock
    private TeamRepository teamRepository;

    public TeamServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateTeam() {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");

        Team team = new Team();
        team.setDestination("Mount Everest");
        team.setDepartureTime(LocalDateTime.of(2024, 6, 15, 8, 0));
        team.setMaxMembers(10);
        team.setAdmin(admin);

        when(teamRepository.save(team)).thenReturn(team);

        Team createdTeam = teamService.createTeam(team);

        assertEquals("Mount Everest", createdTeam.getDestination());
        assertEquals(1, createdTeam.getCurrentMembers());
        assertEquals(admin, createdTeam.getAdmin());
    }
}

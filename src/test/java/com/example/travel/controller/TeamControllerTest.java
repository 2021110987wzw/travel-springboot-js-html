package com.example.travel.controller;

import com.example.travel.entity.Team;
import com.example.travel.entity.User;
import com.example.travel.service.TeamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeamController.class)
public class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TeamService teamService;

    @Test
    public void testCreateTeam() throws Exception {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");

        Team team = new Team();
        team.setDestination("Mount Everest");
        team.setDepartureTime(LocalDateTime.of(2024, 6, 15, 8, 0));
        team.setMaxMembers(10);
        team.setAdmin(admin);
        team.setCurrentMembers(1);

        when(teamService.createTeam(any(Team.class))).thenReturn(team);

        mockMvc.perform(post("/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"destination\": \"Mount Everest\", \"departureTime\": \"2024-06-15T08:00:00\", \"maxMembers\": 10, \"admin\": {\"id\": 1}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destination").value("Mount Everest"))
                .andExpect(jsonPath("$.currentMembers").value(1))
                .andExpect(jsonPath("$.admin.id").value(1));
    }
}

package com.example.travel.controller;

import com.example.travel.entity.TeamMember;
import com.example.travel.service.TeamMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/team-members")
public class TeamMemberController {

    @Autowired
    private TeamMemberService teamMemberService;

    @GetMapping
    public List<TeamMember> getAllTeamMembers() {
        return teamMemberService.getAllTeamMembers();
    }

    @GetMapping("/{id}")
    public TeamMember getTeamMemberById(@PathVariable Long id) {
        return teamMemberService.getTeamMemberById(id);
    }

    @PostMapping
    public TeamMember createTeamMember(@RequestBody TeamMember teamMember) {
        return teamMemberService.createTeamMember(teamMember);
    }

    @DeleteMapping("/{id}")
    public void deleteTeamMember(@PathVariable Long id) {
        teamMemberService.deleteTeamMember(id);
    }
}

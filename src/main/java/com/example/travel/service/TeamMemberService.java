package com.example.travel.service;

import com.example.travel.entity.TeamMember;
import com.example.travel.repository.TeamMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamMemberService {

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    public List<TeamMember> getAllTeamMembers() {
        return teamMemberRepository.findAll();
    }

    public TeamMember getTeamMemberById(Long id) {
        return teamMemberRepository.findById(id).orElse(null);
    }

    public TeamMember createTeamMember(TeamMember teamMember) {
        return teamMemberRepository.save(teamMember);
    }

    public void deleteTeamMember(Long id) {
        teamMemberRepository.deleteById(id);
    }

    @Transactional
    public void deleteTeamMember(Long teamId, Long userId) {
        teamMemberRepository.deleteByTeamIdAndUserId(teamId, userId);
    }
}

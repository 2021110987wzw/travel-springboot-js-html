package com.example.travel.service;

import com.example.travel.entity.Team;
import com.example.travel.entity.TeamMember;
import com.example.travel.entity.User;
import com.example.travel.repository.TeamMemberRepository;
import com.example.travel.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    public Team createTeam(Team team) {
        team.setCurrentMembers(0); // 初始成员数为0
        team.setClosed(false); // 默认队伍是开放的
        return teamRepository.save(team);
    }

    public void addTeamMember(Long teamId, Long userId, String role) {
        TeamMember teamMember = new TeamMember();
        teamMember.setTeamId(teamId);
        teamMember.setUserId(userId);
        teamMember.setRole(role);
        teamMemberRepository.save(teamMember);
    }

    public Team getTeamById(Long id) {
        return teamRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteTeam(Long teamId) {
        // 先删除 team_member 表中的关联数据
        teamMemberRepository.deleteByTeamId(teamId);
        // 再删除 team 表中的数据
        teamRepository.deleteById(teamId);
    }

    @Transactional
    public void updateCurrentMembers(Long teamId) {
        teamRepository.updateCurrentMembers(teamId);
    }

    @Transactional
    public void removeMemberFromTeam(Long teamId, Long userId) {
        // 删除 team_member 表中对应的记录
        teamMemberRepository.deleteByTeamIdAndUserId(teamId, userId);

        // 更新 team 表中的 current_members 数量
        Team team = teamRepository.findById(teamId).orElseThrow();
        team.setCurrentMembers(team.getCurrentMembers() - 1);
        teamRepository.save(team);
    }

    public Team updateTeam(Team team) {
        // Retrieve existing team from the repository
        Team existingTeam = teamRepository.findById(team.getId()).orElse(null);

        if (existingTeam != null) {
            // Update team information
            existingTeam.setDestination(team.getDestination());
            existingTeam.setDepartureTime(team.getDepartureTime());
            existingTeam.setMaxMembers(team.getMaxMembers());
            existingTeam.setCurrentMembers(team.getCurrentMembers()); // 更新 currentMembers 字段

            // 更新 isClosed 字段
            if (team.isClosed() != existingTeam.isClosed()) {
                existingTeam.setClosed(team.isClosed());
            }

            // Save the updated team to the repository
            return teamRepository.save(existingTeam);
        } else {
            return null; // Team does not exist
        }
    }

    @Transactional
    public void updateIsClosed(Long teamId) {
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        if (optionalTeam.isPresent()) {
            Team team = optionalTeam.get();
            // 检查当前成员数是否小于最大成员数，更新isclosed字段
            if (team.getCurrentMembers() < team.getMaxMembers()) {
                team.setClosed(false); // 将isclosed字段设为0
            }

            teamRepository.save(team);
        } else {
            throw new RuntimeException("Team not found with id: " + teamId);
        }
    }
}

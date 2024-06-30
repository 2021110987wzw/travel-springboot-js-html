package com.example.travel.service;

import com.example.travel.entity.Team;
import com.example.travel.entity.TeamMember;
import com.example.travel.entity.User;
import com.example.travel.repository.TeamMemberRepository;
import com.example.travel.repository.TeamRepository;
import com.example.travel.repository.UserRepository;
import com.example.travel.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       TeamRepository teamRepository, TeamMemberRepository teamMemberRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User createUser(User user) throws Exception {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new Exception("Username is already taken");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public boolean authenticateUser(User user) {
        User storedUser = userRepository.findByUsername(user.getUsername());
        return storedUser != null && passwordEncoder.matches(user.getPassword(), storedUser.getPassword());
    }

    public List<Team> getCreatedTeams(Long userId) {
        List<TeamMember> teamMembers = teamMemberRepository.findByUserIdAndRole(userId, "admin");
        List<Team> createdTeams = new ArrayList<>();
        for (TeamMember teamMember : teamMembers) {
            Team team = teamRepository.findById(teamMember.getTeamId()).orElse(null);
            createdTeams.add(team);

            System.out.println(team.getId());
        }
        return createdTeams;
    }

    public List<Team> getJoinedTeams(Long userId) {
        List<TeamMember> teamMembers = teamMemberRepository.findByUserIdAndRole(userId, "member");
        List<Team> joinedTeams = new ArrayList<>();
        for (TeamMember teamMember : teamMembers) {
            Team team = teamRepository.findById(teamMember.getTeamId()).orElse(null);
            joinedTeams.add(team);

            System.out.println(team.getId());
        }
        return joinedTeams;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("USER")
                .build();
    }

    public User getCurrentUser(String token) {
        String username = JwtUtil.extractUsername(token);
        return userRepository.findByUsername(username);
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

}

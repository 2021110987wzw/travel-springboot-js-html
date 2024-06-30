package com.example.travel.service;

import com.example.travel.entity.JoinRequest;
import com.example.travel.entity.Team;
import com.example.travel.entity.TeamMember;
import com.example.travel.entity.User;
import com.example.travel.repository.JoinRequestRepository;
import com.example.travel.repository.TeamRepository;
import com.example.travel.repository.TeamMemberRepository;
import com.example.travel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JoinRequestService {

    @Autowired
    private JoinRequestRepository joinRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    public List<JoinRequest> getAllJoinRequests() {
        return joinRequestRepository.findAll();
    }

    public JoinRequest getJoinRequestById(Long id) {
        return joinRequestRepository.findById(id).orElse(null);
    }

    public JoinRequest createJoinRequest(JoinRequest joinRequest) {
        // Check if the user and team exist
        User user = userRepository.findById(joinRequest.getUser().getId()).orElse(null);
        Team team = teamRepository.findById(joinRequest.getTeam().getId()).orElse(null);

        if (user == null || team == null) {
            throw new IllegalArgumentException("Invalid user or team ID");
        }

        joinRequest.setUser(user);
        joinRequest.setTeam(team);
        joinRequest.setApproved(false);

        return joinRequestRepository.save(joinRequest);
    }

    public JoinRequest processJoinRequest(Long id, boolean approved) {
        JoinRequest joinRequest = joinRequestRepository.findById(id).orElse(null);

        if (joinRequest == null) {
            throw new IllegalArgumentException("Join request not found");
        }

        // Get the user and team
        User user = joinRequest.getUser();
        Team team = joinRequest.getTeam();

        if (user == null || team == null) {
            throw new IllegalArgumentException("Invalid user or team in join request");
        }

        if (approved) {
            if (team.isClosed()) {
                throw new IllegalArgumentException("Team is closed for new members");
            }

            joinRequest.setApproved(true);
            joinRequestRepository.save(joinRequest);

            // Add user to the team
            TeamMember teamMember = new TeamMember();
            teamMember.setUser(user);
            teamMember.setTeam(team);
            teamMemberRepository.save(teamMember);

            // Update currentMembers and check if the team should be closed
            team.setCurrentMembers(team.getCurrentMembers() + 1);
            if (team.getCurrentMembers() >= team.getMaxMembers()) {
                team.setClosed(true);
            }
            teamRepository.save(team);
        } else {
            joinRequestRepository.deleteById(id);
        }

        return joinRequest;
    }

    public void deleteJoinRequest(Long id) {
        joinRequestRepository.deleteById(id);
    }
}

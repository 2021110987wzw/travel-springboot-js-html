package com.example.travel.controller;

import com.example.travel.entity.JoinRequest;
import com.example.travel.entity.Team;
import com.example.travel.entity.TeamMember;
import com.example.travel.entity.User;
import com.example.travel.service.TeamService;
import com.example.travel.repository.JoinRequestRepository;
import com.example.travel.repository.TeamMemberRepository;
import com.example.travel.repository.TeamRepository;
import com.example.travel.repository.UserRepository;
import com.example.travel.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
public class JoinRequestController {

    private final JoinRequestRepository joinRequestRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final JwtUtil jwtUtil;
    private final TeamService teamService;

    @Autowired
    public JoinRequestController(JoinRequestRepository joinRequestRepository, UserRepository userRepository,
                                 TeamRepository teamRepository, TeamMemberRepository teamMemberRepository, JwtUtil jwtUtil, TeamService teamService) {
        this.joinRequestRepository = joinRequestRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.jwtUtil = jwtUtil;
        this.teamService = teamService;
    }
    //提交加入请求的端点
    @PostMapping("/join-request")
    public ResponseEntity<String> joinRequest(@RequestBody JoinRequest request, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");
        String username = jwtUtil.extractUsername(token);

        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        Team team = request.getTeam();
        if (team == null || team.getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Team ID is required");
        }

        Optional<Team> teamOpt = teamRepository.findById(team.getId());
        if (!teamOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Team not found");
        }
        team = teamOpt.get();

        Optional<TeamMember> teamMemberOpt = teamMemberRepository.findByUserIdAndTeamId(user.getId(), team.getId());
        if (teamMemberOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User is already a member of the team");
        }

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setUser(user);
        joinRequest.setTeam(team);
        joinRequest.setCreatedAt(LocalDateTime.now());
        joinRequestRepository.save(joinRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body("Join request submitted successfully");
    }
    //获取所有加入请求的端点
    @GetMapping("/join-requests")
    public ResponseEntity<Map<String, List<JoinRequest>>> getAllJoinRequests(HttpServletRequest request) {
        // 解析 JWT Token 获取用户名
        String token = request.getHeader("Authorization");
        String username = jwtUtil.extractUsername(token);

        // 根据用户名查询用户的 ID
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Long userId = user.getId();

        // 查询用户管理的队伍和发出的申请
        List<Long> managedTeamIds = teamRepository.findManagedTeamIds(userId);
        List<Long> requestedTeamIds = joinRequestRepository.findRequestedTeamIds(userId);

        // 查询加入请求
        List<JoinRequest> managedJoinRequests = joinRequestRepository.findByTeamIdIn(managedTeamIds);
        List<JoinRequest> requestedJoinRequests = joinRequestRepository.findByUserId(userId);

        // 设置用户名和队伍名
        for (JoinRequest joinRequest : managedJoinRequests) {
            joinRequest.setUserDisplayName(joinRequest.getUser().getUsername());
            joinRequest.setTeamDisplayName(joinRequest.getTeam().getTeamName());
        }
        for (JoinRequest joinRequest : requestedJoinRequests) {
            joinRequest.setUserDisplayName(joinRequest.getUser().getUsername());
            joinRequest.setTeamDisplayName(joinRequest.getTeam().getTeamName());
        }

        // 构造返回的结果
        Map<String, List<JoinRequest>> joinRequestsMap = new HashMap<>();
        joinRequestsMap.put("managedTeams", managedJoinRequests);
        joinRequestsMap.put("requestedTeams", requestedJoinRequests);

        return ResponseEntity.ok(joinRequestsMap);
    }
    //处理加入请求的端点
    @PutMapping("/join-request/process/{id}")
    public ResponseEntity<String> processJoinRequest(@PathVariable Long id, @RequestParam boolean approved, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");
        String username = jwtUtil.extractUsername(token);

        Optional<User> adminUserOpt = Optional.ofNullable(userRepository.findByUsername(username));
        if (!adminUserOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin user not found");
        }

        Optional<JoinRequest> joinRequestOpt = joinRequestRepository.findById(id);
        if (!joinRequestOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Join request not found");
        }

        JoinRequest joinRequest = joinRequestOpt.get();
        if (joinRequest.isProcessed()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Join request already processed");
        }

        joinRequest.setApproved(approved);
        joinRequest.setProcessed(true);
        joinRequestRepository.save(joinRequest);

        if (approved) {
            // 如果同意加入请求，则执行将用户加入队伍的逻辑
            User user = joinRequest.getUser();
            Team team = joinRequest.getTeam();

            // 模拟将用户加入队伍的逻辑，实际项目中可能需要根据具体业务逻辑处理
            TeamMember teamMember = new TeamMember();
            teamMember.setUser(user);
            teamMember.setTeam(team);
            teamMember.setJoinedAt(LocalDateTime.now());
            teamMemberRepository.save(teamMember);

            // 更新队伍的当前成员数量
            team.setCurrentMembers(team.getCurrentMembers() + 1);

            // 如果当前成员数量达到最大成员数量，则关闭队伍
            if (team.getCurrentMembers() >= team.getMaxMembers()) {
                team.setClosed(true);
            }

            teamService.updateTeam(team);

            return ResponseEntity.ok("Join request approved successfully");
        } else {
            // 如果拒绝加入请求，可以根据需求执行相应的操作，例如记录拒绝原因等
            return ResponseEntity.ok("Join request rejected");
        }
    }
}

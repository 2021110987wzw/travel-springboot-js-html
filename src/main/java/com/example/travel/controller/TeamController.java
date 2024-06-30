package com.example.travel.controller;

import com.example.travel.entity.Team;
import com.example.travel.entity.TeamMember;
import com.example.travel.entity.User;
import com.example.travel.repository.TeamRepository;
import com.example.travel.service.TeamService;
import com.example.travel.service.TeamMemberService;
import com.example.travel.service.UserService;
import com.example.travel.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private TeamMemberService teamMemberService;

    @Autowired
    private UserService userService;

    @Autowired
    private TeamRepository teamRepository;

    @GetMapping("/all-teams")
    public List<Team> getAllTeams() {
        return teamRepository.findAllTeams();
    }

//    @PostMapping("/createteams")
//    public ResponseEntity<String> createTeam(@RequestBody Team team) {
//        // 设置队伍的管理员为当前登录用户
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String currentPrincipalName = authentication.getName();
//        User admin = userService.findByUsername(currentPrincipalName);
//        team.setAdmin(admin);
//
//        Team createdTeam = teamService.createTeam(team);
//        if (createdTeam != null) {
//            return ResponseEntity.status(HttpStatus.CREATED).body("队伍创建成功！");
//        } else {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("队伍创建失败，请重试。");
//        }
//    }
    //获取队伍成员端点
    @GetMapping("/{teamId}/members")
    public ResponseEntity<List<Map<String, Object>>> getTeamMembers(@PathVariable Long teamId) {
        List<Map<String, Object>> membersDetails = teamRepository.findTeamMembersDetails(teamId);
        return ResponseEntity.ok(membersDetails);
    }
    //删除队伍成员端点
    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<String> removeTeamMember(@PathVariable Long teamId, @PathVariable Long userId, HttpServletRequest request) {
        // 获取当前登录用户的用户名
        String token = request.getHeader("Authorization");
        String username = JwtUtil.extractUsername(token);
        User admin = userService.findByUsername(username);

        // 检查用户是否是队伍管理员
        Team existingTeam = teamService.getTeamById(teamId);
        if (existingTeam == null || !existingTeam.getAdmin().equals(admin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("您没有权限删除该队伍成员。");
        }

        // 删除成员
        teamService.removeMemberFromTeam(teamId, userId);
        return ResponseEntity.status(HttpStatus.OK).body("队伍成员删除成功！");
    }
    //退出队伍端点
    @DeleteMapping("/{teamId}/leave/{userId}")
    public ResponseEntity<?> leaveTeam(@PathVariable Long teamId, @PathVariable Long userId) {
        try {
            // 第一步：从team_member表中删除记录
            teamMemberService.deleteTeamMember(teamId, userId);

            // 第二步：更新team表中的current_members字段
            teamService.updateCurrentMembers(teamId);

            // 第三步：检查当前成员数是否小于最大成员数，更新isclosed字段
            teamService.updateIsClosed(teamId);

            return ResponseEntity.ok("成功退出队伍。");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("退出队伍失败。");
        }
    }
    //获取指定队伍端点
    @GetMapping("/get-team/{id}")
    public ResponseEntity<?> getTeamById(@PathVariable Long id, HttpServletRequest request) {
        Team team = teamService.getTeamById(id);
        if (team == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Team not found.");
        }

        // 从请求头中获取 token 并解析用户名
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token not provided.");
        }

        String username = JwtUtil.extractUsername(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid token.");
        }

        User admin = userService.findByUsername(username);
        if (admin == null || !team.getAdmin().equals(admin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to view this team.");
        }

        return ResponseEntity.ok(team);
    }
    //更新队伍信息端点
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateTeam(@PathVariable Long id, @RequestBody Team updatedTeam, HttpServletRequest request) {
        Team existingTeam = teamService.getTeamById(id);
        if (existingTeam == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("找不到指定的队伍。");
        }

        // 从请求头中获取 token 并解析用户名
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token not provided.");
        }

        String username = JwtUtil.extractUsername(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid token.");
        }

        User admin = userService.findByUsername(username);
        if (admin == null || !existingTeam.getAdmin().equals(admin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("您没有权限修改该队伍信息。");
        }

        // 更新队伍信息
        existingTeam.setTeamName(updatedTeam.getTeamName());
        existingTeam.setTeamDescription(updatedTeam.getTeamDescription());
        existingTeam.setDestination(updatedTeam.getDestination());
        existingTeam.setDepartureTime(updatedTeam.getDepartureTime());

        int newMaxMembers = updatedTeam.getMaxMembers();
        if (newMaxMembers < existingTeam.getCurrentMembers()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("新的最大成员数不能小于当前成员数。");
        }
        existingTeam.setMaxMembers(newMaxMembers);

        // 自动关闭队伍
        existingTeam.setClosed(newMaxMembers == existingTeam.getCurrentMembers());

        Team updatedTeamEntity = teamService.updateTeam(existingTeam);
        if (updatedTeamEntity != null) {
            return ResponseEntity.status(HttpStatus.OK).body("队伍信息更新成功！");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("队伍信息更新失败，请重试。");
        }
    }
    //删除队伍端点
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTeam(@PathVariable Long id, HttpServletRequest request) {
        // 获取当前登录用户的用户名
        String token = request.getHeader("Authorization");
        String username = JwtUtil.extractUsername(token);

        User admin = userService.findByUsername(username);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("您没有权限删除该队伍。");
        }

        Team existingTeam = teamService.getTeamById(id);
        if (existingTeam == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("找不到指定的队伍。");
        }

        // 检查当前用户是否是队伍的管理员
        if (!existingTeam.getAdmin().equals(admin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("您没有权限删除该队伍。");
        }

        teamService.deleteTeam(id);
        return ResponseEntity.status(HttpStatus.OK).body("队伍删除成功！");
    }
}
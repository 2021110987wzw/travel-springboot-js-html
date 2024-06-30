package com.example.travel.controller;

import com.example.travel.entity.Team;
import com.example.travel.entity.User;
import com.example.travel.service.TeamService;
import com.example.travel.service.UserService;
import com.example.travel.util.JwtUtil;
import com.example.travel.repository.TeamRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.ArrayList;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private TeamRepository teamRepository;
    //更新个人资料端点
    @PostMapping("/update-profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "avatarUrl", required = false) String avatarUrl,
            @RequestParam(value = "customAvatar", required = false) MultipartFile customAvatar) {
        try {
            User user = userService.getCurrentUser(token);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            if (!StringUtils.isEmpty(username)) {
                user.setUsername(username);
            }

            if (!StringUtils.isEmpty(email)) {
                user.setEmail(email);
            }

            if (!StringUtils.isEmpty(gender)) {
                user.setGender(User.Gender.valueOf(gender));
            }

            if (customAvatar != null && !customAvatar.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + customAvatar.getOriginalFilename();
                String filePath = "C:\\Users\\Lenovo\\IdeaProjects\\travel\\src\\main\\resources\\static\\avatar-uploads\\" + fileName; // 请替换为实际的保存路径
                Files.copy(customAvatar.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
                user.setAvatarUrl(filePath);
            } else if (!StringUtils.isEmpty(avatarUrl)) {
                user.setAvatarUrl(avatarUrl);
            }

            userService.updateUser(user);

            return ResponseEntity.ok("Profile updated successfully");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving avatar: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating profile: " + e.getMessage());
        }
    }
    //获取用户个人资料端点
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String username = JwtUtil.extractUsername(token);

        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        Long userId = user.getId();
        List<Team> createdTeams = userService.getCreatedTeams(userId);
        List<Team> joinedTeams = userService.getJoinedTeams(userId);

        // Fetch detailed member information for each created team
        List<Map<String, Object>> createdTeamsDetails = new ArrayList<>();
        for (Team team : createdTeams) {
            Map<String, Object> teamInfo = new HashMap<>();
            teamInfo.put("id", team.getId()); // 添加队伍ID
            teamInfo.put("teamName", team.getTeamName());
            teamInfo.put("destination", team.getDestination());
            teamInfo.put("departureTime", team.getDepartureTime());
            teamInfo.put("maxMembers", team.getMaxMembers());
            teamInfo.put("currentMembers", team.getCurrentMembers());
            teamInfo.put("teamDescription", team.getTeamDescription());

            // Fetch members details for the current team
            List<Map<String, Object>> membersDetails = teamRepository.findTeamMembersDetails(team.getId());
            teamInfo.put("members", membersDetails);

            createdTeamsDetails.add(teamInfo);
        }

        List<Map<String, Object>> joinedTeamsDetails = new ArrayList<>();
        for (Team team : joinedTeams) {
            Map<String, Object> teamInfo = new HashMap<>();
            teamInfo.put("id", team.getId()); // 添加队伍ID
            teamInfo.put("teamName", team.getTeamName());
            teamInfo.put("destination", team.getDestination());
            teamInfo.put("departureTime", team.getDepartureTime());
            teamInfo.put("maxMembers", team.getMaxMembers());
            teamInfo.put("currentMembers", team.getCurrentMembers());
            teamInfo.put("teamDescription", team.getTeamDescription());

            // Fetch members details for the current team
            List<Map<String, Object>> membersDetails = teamRepository.findTeamMembersDetails(team.getId());
            teamInfo.put("members", membersDetails);

            joinedTeamsDetails.add(teamInfo);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("avatarUrl", user.getAvatarUrl());
        response.put("gender", user.getGender().toString());
        response.put("createdTeams", createdTeamsDetails);
        response.put("joinedTeams", joinedTeamsDetails);

        return ResponseEntity.ok(response);
    }
    //创建队伍端点
    @PostMapping("/create-team")
    public ResponseEntity<?> createTeam(@RequestBody Team team, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            String username = JwtUtil.extractUsername(token);
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            team.setAdmin(user); // 设置队伍管理员为当前用户
            team.setCurrentMembers(1); // 设置当前成员数为 1
            Team createdTeam = teamService.createTeam(team); // 创建队伍
            if (createdTeam != null) {
                teamService.addTeamMember(createdTeam.getId(), user.getId(), "admin"); // 添加创建者到队伍成员列表中，角色为管理员
                createdTeam.setCurrentMembers(1); // 再次明确设置成员数
                teamService.updateTeam(createdTeam); // 保存更新后的队伍
                return ResponseEntity.status(HttpStatus.CREATED).body(createdTeam);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating team");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating team: " + e.getMessage());
        }
    }
    //注册用户端点
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestParam("username") String username,
                                          @RequestParam("password") String password,
                                          @RequestParam("email") String email,
                                          @RequestParam("gender") String gender,
                                          @RequestParam(value = "avatarUrl", required = false) String avatarUrl,
                                          @RequestParam(value = "customAvatar", required = false) MultipartFile customAvatar) {
        try {
            String avatarPath = avatarUrl;
            if (customAvatar != null && !customAvatar.isEmpty()) {
                // 生成自定义头像的文件名
                String fileName = UUID.randomUUID().toString() + "_" + customAvatar.getOriginalFilename();
                // 构建自定义头像文件保存路径
                String filePath = "C:\\Users\\Lenovo\\IdeaProjects\\travel\\src\\main\\resources\\static\\avatar-uploads\\" + fileName;
                // 将自定义头像保存到文件系统
                Files.copy(customAvatar.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
                // 设置用户头像路径为自定义头像的文件路径
                avatarPath = filePath;
            }

            // 创建用户对象并设置属性
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email);
            user.setGender(User.Gender.valueOf(gender));
            user.setAvatarUrl(avatarPath);

            // 调用 userService 的方法创建用户
            User registeredUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (DuplicateKeyException e) {
            // 处理用户名重复异常
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username is already taken. Please choose a different username.");
        } catch (Exception e) {
            // 处理其他异常
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error registering user: " + e.getMessage());
        }
    }

   //用户登录端点
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        if (userService.authenticateUser(user)) {
            System.out.println(user.getUsername());
            String token = JwtUtil.generateToken(user.getUsername()); // 生成Token
            Map<String, String> response = new HashMap<>();
            response.put("message", "Login successful!");
            response.put("token", token); // 将Token添加到响应中
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed. Invalid username or password.");
        }
    }
}
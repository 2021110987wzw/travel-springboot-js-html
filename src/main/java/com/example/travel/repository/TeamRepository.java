package com.example.travel.repository;

import com.example.travel.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("SELECT t FROM Team t ")
    List<Team> findAllTeams();

    // 查询用户是管理员的队伍的 ID 列表
    @Query("SELECT t.id FROM Team t WHERE t.admin.id = :userId")
    List<Long> findManagedTeamIds(Long userId);

    @Query("SELECT tm.user.id AS userId, tm.user.username AS username, tm.user.email AS email, tm.role AS role " +
            "FROM TeamMember tm " +
            "WHERE tm.team.id = :teamId")
    List<Map<String, Object>> findTeamMembersDetails(@Param("teamId") Long teamId);

    @Modifying
    @Query("UPDATE Team t SET t.currentMembers = t.currentMembers - 1 WHERE t.id = :teamId")
    void updateCurrentMembers(@Param("teamId") Long teamId);

}

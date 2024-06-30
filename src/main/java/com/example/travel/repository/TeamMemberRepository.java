package com.example.travel.repository;

import com.example.travel.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional; // 导入 Optional 类

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    @Query("SELECT tm FROM TeamMember tm WHERE tm.user.id = :userId AND tm.role = :role")
    List<TeamMember> findByUserIdAndRole(@Param("userId") Long userId, @Param("role") String role);

    Optional<TeamMember> findByUserIdAndTeamId(Long userId, Long teamId);

    void deleteByTeamId(Long teamId);

    void deleteByTeamIdAndUserId(Long teamId, Long userId);

//    @Modifying
//    @Query("DELETE FROM TeamMember tm WHERE tm.teamId = :teamId AND tm.userId = :userId")
//    void deleteByTeamIdAndUserId2(@Param("teamId") Long teamId, @Param("userId") Long userId);
}

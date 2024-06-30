package com.example.travel.repository;

import com.example.travel.entity.JoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    // 查询用户发出的加入请求所对应的队伍的 ID 列表
    @Query("SELECT jr.team.id FROM JoinRequest jr WHERE jr.user.id = :userId")
    List<Long> findRequestedTeamIds(Long userId);

    // 根据队伍的 ID 列表查询加入请求
    List<JoinRequest> findByTeamIdIn(List<Long> teamIds);

    // 根据用户的 ID 查询用户发出的所有加入请求
    List<JoinRequest> findByUserId(Long userId);
}

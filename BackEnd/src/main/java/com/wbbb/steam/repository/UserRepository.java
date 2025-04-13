package com.wbbb.steam.repository;

import com.wbbb.steam.dto.response.data.UserDto;
import com.wbbb.steam.dto.response.data.UserSimpleDto;
import com.wbbb.steam.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 基础查询
    User findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // 普通用户搜索（只返回基本信息）
    @Query("SELECT new com.wbbb.steam.dto.response.data.UserSimpleDto(u.userId, u.username, u.nickname, u.avatar) FROM User u " +
            "WHERE LOWER(u.nickname) LIKE CONCAT('%', LOWER(:keyword) , '%') " +
            "OR u.username = :keyword " +
            "OR u.userId = :keyword")
    Page<UserSimpleDto> searchUserBasic(String keyword, Pageable pageable);

    // 管理员搜索（返回完整信息）
    @Query("SELECT new com.wbbb.steam.dto.response.data.UserDto(u.userId, u.username, u.email, u.nickname, u.avatar, u.createTime, u.role, u.enabled) FROM User u " +
            "WHERE LOWER(u.nickname) LIKE CONCAT('%', LOWER(:keyword) , '%') " +
            "OR u.username = :keyword " +
            "OR u.userId = :keyword")
    Page<UserDto> searchUserAdmin(String keyword, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "CAST(u.userId AS string) LIKE CONCAT('%', :keyword, '%')")
    Page<User> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
           "(:username IS NULL OR u.username LIKE %:username%) AND " +
           "(:email IS NULL OR u.email LIKE %:email%)")
    Page<User> findByUsernameContaining(String username, Pageable pageable);

    Page<User> findByEmailContaining(String email, Pageable pageable);

    Page<User> findByUsernameContainingAndEmailContaining(String username, String email, Pageable pageable);
    Page<User> findByUsernameContainingOrNicknameContaining(String keyword, String keyword2, PageRequest of);
}

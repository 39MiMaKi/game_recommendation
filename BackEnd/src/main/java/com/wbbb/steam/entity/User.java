package com.wbbb.steam.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.annotations.CreationTimestamp;


/**
 * 账户信息
 */

@Data
@Entity
@NoArgsConstructor
@Table(name = "users")
        public class User {

    /**
     * 用户偏好向量
     */
    @ElementCollection
    @CollectionTable(name = "user_preference", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "preference_key")
    @Column(name = "preference_value")
    private Map<String, Double> preferenceVector = new HashMap<>();

    /**
     * 账户id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "user_id_generator")
    @TableGenerator(name = "user_id_generator", table = "id_generator", pkColumnName = "name", valueColumnName = "value",
            pkColumnValue = "user", initialValue = 1463960129)
    @Column(name = "user_id")
    private Long userId;

    /**
     * 邮箱
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * 账户名称
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * 密码
     */
    @Column(nullable = false, length = 64)
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 创建时间
     */
   
    @CreationTimestamp
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /**
     * 用户偏好标签
     */
    @Column(nullable = false, length = 512)
    private String tags;

    /**
     * 用户角色，0表示普通用户，1表示管理员
     */
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer role = 0;

    /**
     * 账户启用状态
     */
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean enabled = true;

    // Builder中增加enabled字段
    @Builder
    public User(String email, String username, String password, String nickname, String avatar, LocalDateTime createTime, String tags, Map<String, Double> preferenceVector, Integer role) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.avatar = avatar;
        this.createTime = createTime;
        this.tags = tags != null ? tags : "";
        this.preferenceVector = preferenceVector != null ? preferenceVector : new HashMap<>();
        if (role != null) {
            this.role = role;
        }
        this.enabled = enabled != null ? enabled : true;
    }
}

package com.wbbb.steam.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user_rating")
public class UserRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Long appId;
    
    @Column(nullable = false)
    private Boolean recommended;
    
    @Column(length = 1000)
    private String comment;
    
    @Column(nullable = false)
    private Long timestamp;
    
    @Builder
    public UserRating(Long id, Long userId, Long appId, Boolean recommended, String comment, Long timestamp) {
        this.id = id;
        this.userId = userId;
        this.appId = appId;
        this.recommended = recommended;
        this.comment = comment;
        this.timestamp = timestamp != null ? timestamp : System.currentTimeMillis();
    }
}
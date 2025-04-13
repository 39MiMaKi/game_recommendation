package com.wbbb.steam.dto.response.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponseDto {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String tags;
    private LocalDateTime createTime;
    
    // Add this constructor to match the one being used in UserController
    public UserInfoResponseDto(Long userId, String username, String nickname, String avatar) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.avatar = avatar;
    }
}

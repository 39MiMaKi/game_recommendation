package com.wbbb.steam.dto.response.data;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserDto {
    private Long userId;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private LocalDateTime createTime;
    private Integer role;
    private Boolean enabled;


}

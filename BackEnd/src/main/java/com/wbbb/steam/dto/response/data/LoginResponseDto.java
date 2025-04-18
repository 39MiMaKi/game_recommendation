package com.wbbb.steam.dto.response.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * /user/login 响应数据
 */
@Getter
@Setter
@AllArgsConstructor
public class LoginResponseDto {
    private String token;
    private Long userId;
    private Integer role; // 0-普通用户 1-管理员
}

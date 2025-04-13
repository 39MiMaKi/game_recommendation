package com.wbbb.steam.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * /user/join 请求
 */
@Getter
@Setter
public class JoinRequestDto {
    private String email;
    private String username;
    private String password;
    private List<String> tags;

}

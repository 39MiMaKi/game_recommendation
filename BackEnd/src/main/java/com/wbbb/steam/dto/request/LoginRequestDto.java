package com.wbbb.steam.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * /user/login 请求
 */
@Getter
@Setter
public class LoginRequestDto {
 
    private String username;
    

    private String password; // 必须定义为String
    
    private boolean rememberMe;
    
  
}

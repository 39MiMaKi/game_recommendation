package com.wbbb.steam.util;

import java.util.regex.Pattern;

/**
 * 校验工具
 */
public class ValidateUtil {
    /**
     * 校验邮箱地址是否合法
     */
    public static boolean isEmailValid(String email) {
        Pattern regex = Pattern.compile("^[\\w-]+(.[\\w-]+)*@([a-zA-Z0-9]+(-?[a-zA-Z0-9]+)+\\.)+[a-zA-Z]{2,4}$");
        return regex.matcher(email).matches();
    }
    
    /**
     * 校验密码是否合法
     */
    public static boolean isPasswordValid(String password) {
        return Pattern.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$", password);
    }
}

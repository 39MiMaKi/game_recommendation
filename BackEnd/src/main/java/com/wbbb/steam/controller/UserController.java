package com.wbbb.steam.controller;

import com.wbbb.steam.dto.request.JoinRequestDto;
import com.wbbb.steam.dto.request.LoginRequestDto;
import com.wbbb.steam.dto.request.UserProfileUpdateDto;
import com.wbbb.steam.dto.response.PageDto;
import com.wbbb.steam.dto.response.ResponseDto;
import com.wbbb.steam.dto.response.data.LoginResponseDto;
import com.wbbb.steam.dto.response.data.UserGameStatsDto;
import com.wbbb.steam.dto.response.data.UserInfoResponseDto;
import com.wbbb.steam.dto.response.data.UserRatingDto;
import com.wbbb.steam.dto.response.data.UserSimpleDto;
import com.wbbb.steam.entity.User;
import com.wbbb.steam.service.UserService;
import com.wbbb.steam.service.UserStatsService;
import com.wbbb.steam.util.TokenUtil;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;


import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@AllArgsConstructor
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final UserStatsService userStatsService;

    /**
     * 检查账户名称是否可用
     */
    @GetMapping("/available")
    public ResponseDto<Boolean> checkUsernameAvailable(@RequestParam("username") String username) {
        if (username.isEmpty())
            return ResponseDto.success(false);
        return ResponseDto.success(userService.isUsernameAvailable(username));
    }

    /**
     * 检查邮箱是否可用
     */
    @GetMapping("/email/available")
    public ResponseDto<Boolean> checkEmailAvailable(@RequestParam("email") String email) {
        if (email.isEmpty())
            return ResponseDto.success(false);
        return ResponseDto.success(userService.isEmailAvailable(email));
    }

    /**
     * 注册
     */
    @PostMapping("/join")
    public ResponseDto<?> join(@RequestBody @Valid JoinRequestDto joinRequestDto) {
        // 移除手动验证email的逻辑，交给@Valid处理
        userService.join(joinRequestDto.getEmail(), joinRequestDto.getUsername(), 
                       joinRequestDto.getPassword(), joinRequestDto.getTags());
        return ResponseDto.success(null, "注册成功");
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public ResponseDto<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        LoginResponseDto loginResponseDto = userService.login(
            loginRequestDto.getUsername(), 
            loginRequestDto.getPassword(), 
            loginRequestDto.isRememberMe()
        );
        if (loginResponseDto == null) {
            return ResponseDto.unauthorized(null, "请核对您的密码和帐户名称并重试。");
        }
        return ResponseDto.success(loginResponseDto, "登录成功");
    }
    

    /**
     * 获取账户信息
     */
    @GetMapping("/info")
    public ResponseDto<UserInfoResponseDto> getUserInfo(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestHeader(value = "token", required = false) String token) {
        if (userId == null && token == null)
            return ResponseDto.badRequest(null, "请求参数错误");
        Long tokenUserId;
        if (userId == null) {
            tokenUserId = userService.parseToken(token);
            if (tokenUserId == null)
                return ResponseDto.unauthorized(null, "登录状态已失效，请重新登录");
            userId = tokenUserId;
        }
        User user = userService.getUser(userId);
        if (user == null)
            return ResponseDto.notFound(null, "无法找到指定的个人资料");
        return ResponseDto.success(new UserInfoResponseDto(userId, user.getUsername(), user.getNickname(), user.getAvatar()));
    }

    @GetMapping("/search")
    public ResponseDto<PageDto<UserSimpleDto>> searchUser(
            @RequestParam("keyword") String keyword,
            @RequestParam("pageIndex") Integer pageIndex,
            @RequestParam("pageSize") Integer pageSize,
            @RequestHeader("token") String token) {
        if (keyword.isEmpty())
            return ResponseDto.success(new PageDto<>(0L, pageIndex, pageSize, new ArrayList<>()));
        return ResponseDto.success(userService.searchUser(keyword, pageIndex, pageSize));
    }

    /**
     * 获取用户个人资料
     */
    @GetMapping("/profile")
    public ResponseDto<UserInfoResponseDto> getUserProfile(@RequestHeader("Authorization") String token) {
        Long userId = TokenUtil.parseToken(token);
        if (userId == null) {
            return ResponseDto.unauthorized(null, "未登录或登录已过期");
        }
        User user = userService.getUser(userId);
        if (user == null) {
            return ResponseDto.notFound(null, "用户不存在");
        }
        // Fix the success method call by adding a message parameter
        return ResponseDto.success(new UserInfoResponseDto(userId, user.getUsername(), user.getNickname(), user.getAvatar()), "获取个人资料成功");
    }
    
    /**
     * 更新用户个人资料
     */
    @PutMapping("/upadate")
    public ResponseDto<?> updateUserProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UserProfileUpdateDto profileUpdateDto) {
        Long userId = TokenUtil.parseToken(token);
        if (userId == null) {
            return ResponseDto.unauthorized(null, "未登录或登录已过期");
        }
        boolean success = userService.updateUserProfile(userId, profileUpdateDto);
        if (success) {
            return ResponseDto.success(null, "个人资料更新成功");
        } else {
            return ResponseDto.badRequest(null, "个人资料更新失败");
        }
    }

    /**
     * 获取用户游戏评分记录
     */
    @GetMapping("/ratings")
    public ResponseDto<PageDto<UserRatingDto>> getUserRatings(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = TokenUtil.parseToken(token);
        if (userId == null) {
            return ResponseDto.unauthorized(null, "未登录或登录已过期");
        }
        PageDto<UserRatingDto> ratings = userService.getUserRatings(userId, page, size);
        return ResponseDto.success(ratings);
    }
    
    /**
     * 获取用户游戏统计信息
     */
    @GetMapping("/stats")
    public ResponseDto<UserGameStatsDto> getUserGameStats(@RequestHeader("Authorization") String token) {
        Long userId = TokenUtil.parseToken(token);
        if (userId == null) {
            return ResponseDto.unauthorized(null, "未登录或登录已过期");
        }
        UserGameStatsDto stats = userStatsService.getUserGameStats(userId);
        return ResponseDto.success(stats);
    }
}

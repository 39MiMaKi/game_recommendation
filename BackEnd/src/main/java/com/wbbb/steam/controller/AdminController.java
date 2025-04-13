package com.wbbb.steam.controller;

import com.wbbb.steam.dto.response.PageDto;
import com.wbbb.steam.dto.response.ResponseDto;
import com.wbbb.steam.dto.response.data.AppDto;
import com.wbbb.steam.dto.request.AppCreateDto;
import com.wbbb.steam.dto.request.AppUpdateDto;
import com.wbbb.steam.entity.RecommendationConfig;
import com.wbbb.steam.entity.User;
import com.wbbb.steam.service.AdminService;
import com.wbbb.steam.service.AppService;
import com.wbbb.steam.service.RecommendationService;
import com.wbbb.steam.util.TokenUtil;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员控制器
 * 提供管理员后台功能，包括用户管理、游戏管理和推荐系统参数调优
 */
@AllArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final AppService appService; 
    private final RecommendationService recommendationService;

    /******************* 用户管理接口 *******************/
    
    @PostMapping("/users/{userId}/status")
    public ResponseDto<Void> updateUserStatus(
        @PathVariable Long userId,
        @RequestParam Boolean enabled,
        @RequestHeader("token") String token) {
        checkAdmin(token);
        adminService.updateUserStatus(userId, enabled);
        return ResponseDto.success(null, enabled ? "已启用用户" : "已禁用用户");
    }

    @PostMapping("/set-user-role")
    public ResponseDto<Boolean> setUserRole(
        @RequestParam("userId") Long targetUserId,
        @RequestParam("role") Integer role,
        @RequestHeader("token") String token) {
        checkAdmin(token);
        boolean success = adminService.setUserRole(targetUserId, role);
        return success ? 
            ResponseDto.success(true, "角色设置成功") : 
            ResponseDto.badRequest(false, "操作失败");
    }

    @GetMapping("/users")
    public ResponseDto<PageDto<User>> getUsers(
        @RequestParam(value = "page", defaultValue = "0") Integer page,
        @RequestParam(value = "size", defaultValue = "10") Integer size,
        @RequestHeader("token") String token) {
        checkAdmin(token);
        return ResponseDto.success(adminService.getUsers(page, size));
    }

    @GetMapping("/users/search")
    public ResponseDto<PageDto<User>> searchUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String username,
        @RequestParam(required = false) String email,
        @RequestHeader("token") String token) {
        checkAdmin(token);
        return ResponseDto.success(adminService.searchUsers(page, size, username, email));
    }

    /******************* 游戏管理接口 *******************/
    
    @GetMapping("/apps")
    public ResponseDto<PageDto<AppDto>> getApps(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String tag,
        @RequestHeader("token") String token) {
        checkAdmin(token);
        return ResponseDto.success(appService.getApps(page, size, keyword, tag));
    }

    @PutMapping("/apps/{appId}")
    public ResponseDto<Void> updateApp(
        @PathVariable Long appId,
        @RequestBody AppUpdateDto dto,
        @RequestHeader("token") String token) {
        checkAdmin(token);
        adminService.updateApp(appId, dto);
        return ResponseDto.success(null, "更新成功");
    }

    @PostMapping("/apps")
    public ResponseDto<Long> createApp(
        @RequestBody AppCreateDto dto,
        @RequestHeader("token") String token) {
        checkAdmin(token);
        Long appId = adminService.createApp(dto);
        return ResponseDto.success(appId, "游戏创建成功");
    }

    @DeleteMapping("/apps/{appId}")
    public ResponseDto<Void> deleteApp(
        @PathVariable Long appId,
        @RequestHeader("token") String token) {
        checkAdmin(token);
        adminService.deleteApp(appId);
        return ResponseDto.success(null, "游戏删除成功");
    }

    /******************* 推荐系统接口 *******************/
    
    @GetMapping("/recommend-params")
    public ResponseDto<Map<String, Double>> getRecommendParams(
        @RequestHeader("token") String token) {
        checkAdmin(token);
        Map<String, Double> params = adminService.getRecommendationParameters();
        return ResponseDto.success(params);
    }

    @PostMapping("/recommend-params")
    public ResponseDto<Void> updateRecommendParams(
        @RequestBody Map<String, Double> params,
        @RequestHeader("token") String token) {
        checkAdmin(token);
        adminService.updateRecommendationParameters(params);
        return ResponseDto.success(null, "推荐参数更新成功");
    }

    @GetMapping("/recommend-config")
    public ResponseDto<RecommendationConfig> getRecommendConfig(
        @RequestHeader("token") String token) {
        checkAdmin(token);
        return ResponseDto.success(recommendationService.getConfig());
    }

    @PostMapping("/recommend-config")
    public ResponseDto<RecommendationConfig> updateRecommendConfig(
        @RequestBody RecommendationConfig config,
        @RequestHeader("token") String token) {
        checkAdmin(token);
        return ResponseDto.success(recommendationService.updateConfig(config));
    }


    /******************* 内部工具方法 *******************/
    private void checkAdmin(String token) {
        Long userId = TokenUtil.parseToken(token);
        if (userId == null || !adminService.isAdmin(userId)) {
            throw new SecurityException("管理员权限验证失败");
        }
    }
}

package com.wbbb.steam.controller;

import com.wbbb.steam.dto.response.PageDto;
import com.wbbb.steam.dto.response.ResponseDto;
import com.wbbb.steam.dto.response.data.AppDto;
import com.wbbb.steam.service.AppService;


import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.*;


@AllArgsConstructor
@RestController
@RequestMapping("/app")
public class AppController {
    private final AppService appService;

    @GetMapping("/{appId}")
    public ResponseDto<AppDto> getApp(
        @PathVariable Long appId,
        @RequestHeader(value = "token", required = false) String token) {
        return ResponseDto.success(appService.getApp(appId, token));
    }

    @GetMapping
    public ResponseDto<PageDto<AppDto>> getApps(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String tag) {
        return ResponseDto.success(appService.getApps(page, size, keyword, tag));
    }
    
    @GetMapping("/tag/{tag}")
    public ResponseDto<PageDto<AppDto>> getAppsByTag(
        @PathVariable String tag,
        @RequestParam(defaultValue = "0") int pageIndex,
        @RequestParam(defaultValue = "50") int pageSize) {
        return ResponseDto.success(appService.getApps(pageIndex, pageSize, null, tag));
    }
    
    @GetMapping("/recommendations")
    public ResponseDto<List<AppDto>> getRecommendations(
        @RequestHeader(value = "token", required = false) String token) {
        return ResponseDto.success(appService.getRecommendations(token));
    }
    
    /**
     * 获取免费游戏列表
     * @param page 页码
     * @param pageSize 每页数量
     * @param sortBy 排序字段
     * @param tag 标签筛选
     * @param token 用户令牌
     * @return 返回免费游戏列表
     */
    @GetMapping("/free-games")
    public ResponseDto<List<AppDto>> getFreeGames(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "12") int pageSize,
        @RequestParam(defaultValue = "popularity") String sortBy,
        @RequestParam(required = false) String tag,
        @RequestHeader(value = "token", required = false) String token) {
        try {
            // 验证参数
            if (page < 1 || pageSize < 1) {
                return ResponseDto.badRequest(null, "请求参数错误");
            }
            
            // 调用服务层方法获取免费游戏
            // 注意：这里假设appService中有一个getFreeGames方法
            // 实际实现可能需要在AppService中添加相应的方法
            PageDto<AppDto> result = appService.getAppsByPriceRange(
                token, 0.0, 0.0, page - 1, pageSize, sortBy, "DESC");
                
            // 如果有标签筛选，进一步过滤结果
            List<AppDto> freeGames = result.getContent();
            if (tag != null && !tag.isEmpty()) {
                freeGames = freeGames.stream()
                    .filter(app -> app.getTags() != null && app.getTags().contains(tag))
                    .toList();
            }
            
            return ResponseDto.success(freeGames);
        } catch (Exception e) {
            return ResponseDto.badRequest(null, e.getMessage());
        }
    }
}
package com.wbbb.steam.controller;

import com.wbbb.steam.dto.response.PageDto;
import com.wbbb.steam.dto.response.ResponseDto;
import com.wbbb.steam.dto.response.data.UserRatingDto;
import com.wbbb.steam.service.RatingService;
import com.wbbb.steam.service.UserService;
import com.wbbb.steam.util.TokenUtil;

import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rating")
@AllArgsConstructor
public class RatingController {
    private final RatingService ratingService;
    private final UserService userService;

    /**
     * 提交游戏评价
     */
    @PostMapping("/submit")
    public ResponseDto<Boolean> submitRating(
            @RequestHeader("Authorization") String token,
            @RequestParam Long appId,
            @RequestParam Boolean recommended) {
        Long userId = TokenUtil.parseToken(token);
        if (userId == null) {
            return ResponseDto.unauthorized(false, "未登录或登录已过期");
        }
        
        if (recommended == null) {
            return ResponseDto.badRequest(false, "请选择推荐或不推荐");
        }
        
        ratingService.submitRating(userId, appId, recommended);
        
        return ResponseDto.success(true, "评价提交成功");
    }

    /**
     * 获取用户对特定游戏的评价
     */
    @GetMapping("/user")
    public ResponseDto<UserRatingDto> getUserRating(
            @RequestHeader("Authorization") String token,
            @RequestParam("appId") Long appId) {
        Long userId = TokenUtil.parseToken(token);
        if (userId == null) {
            return ResponseDto.unauthorized(null, "未登录或登录已过期");
        }
        
        UserRatingDto rating = ratingService.getUserRating(userId, appId);
        return ResponseDto.success(rating);
    }

    /**
     * 获取游戏的所有评价
     */
    @GetMapping("/app")
    public ResponseDto<PageDto<UserRatingDto>> getAppRatings(
            @RequestParam("appId") Long appId,
            @RequestParam(defaultValue = "1") Integer pageIndex,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        PageDto<UserRatingDto> ratings = ratingService.getAppRatings(appId, pageIndex - 1, pageSize);
        return ResponseDto.success(ratings);
    }

    /**
     * 获取用户的评价历史
     */
    @GetMapping("/user/history")
    public ResponseDto<PageDto<UserRatingDto>> getUserRatingHistory(
            @RequestParam("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer pageIndex,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        PageDto<UserRatingDto> ratings = userService.getUserRatings(userId, pageIndex - 1, pageSize);
        return ResponseDto.success(ratings);
    }



    /**
     * 获取最新的评价记录
     */
    @GetMapping("/recent")
    public ResponseDto<PageDto<UserRatingDto>> getRecentRatings(
            @RequestParam(defaultValue = "1") Integer pageIndex,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        PageDto<UserRatingDto> ratings = ratingService.getRecentRatings(pageIndex - 1, pageSize);
        return ResponseDto.success(ratings);
    }

    /**
     * 删除评价
     */
    @PostMapping("/delete")
    public ResponseDto<Boolean> deleteRating(
            @RequestHeader("Authorization") String token,
            @RequestBody RatingDeleteDto ratingDeleteDto) {
        Long userId = TokenUtil.parseToken(token);
        if (userId == null) {
            return ResponseDto.unauthorized(false, "未登录或登录已过期");
        }
        
        boolean success = ratingService.deleteRating(userId, ratingDeleteDto.getRatingId());
        return success ? 
            ResponseDto.success(true, "评价删除成功") : 
            ResponseDto.badRequest(false, "评价删除失败，可能您没有权限删除此评价");
    }
}



class RatingDeleteDto {
    private Long ratingId;
    
    public Long getRatingId() {
        return ratingId;
    }
    
    public void setRatingId(Long ratingId) {
        this.ratingId = ratingId;
    }
}
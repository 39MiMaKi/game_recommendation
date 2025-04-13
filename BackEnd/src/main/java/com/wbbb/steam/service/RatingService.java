package com.wbbb.steam.service;

import com.wbbb.steam.dto.response.PageDto;
import com.wbbb.steam.dto.response.data.UserRatingDto;
import com.wbbb.steam.entity.App;
import com.wbbb.steam.entity.UserRating;
import com.wbbb.steam.repository.AppRepository;
import com.wbbb.steam.repository.UserRatingRepository;
import com.wbbb.steam.service.UserService;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RatingService {
    private final UserRatingRepository userRatingRepository;
    private final RecommendationService recommendationService;
    private final AppRepository appRepository;
    private final UserService userService;

    public void submitRating(Long userId, Long appId, Boolean recommended) {
        UserRating rating = new UserRating();
        rating.setUserId(userId);
        rating.setAppId(appId);
        rating.setRecommended(recommended);
        rating.setTimestamp(System.currentTimeMillis());
        userRatingRepository.save(rating);
    
        // 更新用户偏好向量
        updateUserPreferenceVector(userId, appId, recommended);
    
        // 更新游戏好评率
        updatePositiveRate(appId);
    }
    
    public UserRatingDto getUserRating(Long userId, Long appId) {
        return userRatingRepository.findByUserIdAndAppId(userId, appId)
                .map(rating -> new UserRatingDto(rating.getAppId(), null, null, rating.getRecommended(), LocalDateTime.ofInstant(Instant.ofEpochMilli(rating.getTimestamp()), ZoneId.systemDefault())))
                .orElse(null);
    }
    
    public PageDto<UserRatingDto> getAppRatings(Long appId, int page, Integer pageSize) {
        Page<UserRating> ratings = userRatingRepository.findByAppId(appId, PageRequest.of(page, pageSize));
        return new PageDto<UserRatingDto>(ratings.getTotalElements(), page, pageSize, ratings.getContent().stream()
                .map(rating -> new UserRatingDto(rating.getAppId(), null, null, rating.getRecommended(), LocalDateTime.ofInstant(Instant.ofEpochMilli(rating.getTimestamp()), ZoneId.systemDefault())))
                .collect(Collectors.toList()));
    }
    

    
    public boolean deleteRating(Long userId, Long ratingId) {
        return userRatingRepository.findById(ratingId)
                .filter(rating -> rating.getUserId().equals(userId))
                .map(rating -> {
                    userRatingRepository.delete(rating);
                    return true;
                })
                .orElse(false);
    }
    
    public PageDto<UserRatingDto> getRecentRatings(int page, Integer pageSize) {
        Page<UserRating> ratings = userRatingRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, pageSize));
        return new PageDto<UserRatingDto>(ratings.getTotalElements(), page, pageSize, ratings.getContent().stream()
                .map(rating -> new UserRatingDto(rating.getAppId(), null, null, rating.getRecommended(), 
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(rating.getTimestamp()), ZoneId.systemDefault())))
                .collect(Collectors.toList()));
    }

    private void updatePositiveRate(Long appId) {
        // 获取游戏的所有评价
        List<UserRating> ratings = userRatingRepository.findByAppId(appId);
        int totalReviews = ratings.size();
        
        if (totalReviews > 0) {
            // 计算好评数量
            long positiveCount = ratings.stream()
                    .filter(UserRating::getRecommended)
                    .count();
            
            // 计算好评率（百分比）
            double positiveRate = (double) positiveCount / totalReviews * 100;
            
            // 更新游戏好评率和评价总数
            appRepository.findById(appId).ifPresent(app -> {
                app.setPositiveRate(positiveRate);
                app.setReviewCount(totalReviews);
                appRepository.save(app);
            });
        }
    }

    private void updateUserPreferenceVector(Long userId, Long appId, Boolean recommended) {
        // 获取游戏标签并更新用户偏好
        Set<String> gameTags = getGameTags(appId);
        if (!gameTags.isEmpty()) {  // 添加空标签检查
            // 在steam风格的点赞/点踩系统中，我们可以使用权重来表示用户偏好
            // 推荐(点赞)=1.0，不推荐(点踩)=0.0
            Double weight = recommended ? 1.0 : 0.0;
            recommendationService.updateUserPreferences(userId, gameTags, weight);
        }
    }

    private Set<String> getGameTags(Long appId) {
        return appRepository.findById(appId)
                .map(App::getTags)
                .orElse(new HashSet<>());
    }
}
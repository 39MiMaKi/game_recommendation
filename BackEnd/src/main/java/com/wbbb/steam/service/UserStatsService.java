package com.wbbb.steam.service;

import com.wbbb.steam.dto.response.data.UserGameStatsDto;
import com.wbbb.steam.entity.User;
import com.wbbb.steam.repository.UserRatingRepository;
import com.wbbb.steam.repository.UserRepository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserStatsService {
    private final UserRepository userRepository;
    private final UserRatingRepository userRatingRepository;
    
    /**
     * 获取用户游戏统计信息
     */
    public UserGameStatsDto getUserGameStats(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }
        
        UserGameStatsDto stats = new UserGameStatsDto();
        stats.setUserId(userId);
        stats.setUsername(user.getUsername());
        
        // 获取评分游戏数量
        Integer ratedGamesCount = userRatingRepository.countByUserId(userId);
        stats.setRatedGamesCount(ratedGamesCount != null ? ratedGamesCount : 0);
        
        return stats;
    }
}
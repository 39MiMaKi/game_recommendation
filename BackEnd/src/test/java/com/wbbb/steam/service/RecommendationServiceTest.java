package com.wbbb.steam.service;

import com.wbbb.steam.entity.App;
import com.wbbb.steam.entity.User;
import com.wbbb.steam.entity.UserRating;
import com.wbbb.steam.repository.AppRepository;
import com.wbbb.steam.repository.UserRatingRepository;
import com.wbbb.steam.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
public class RecommendationServiceTest {

    @Mock
    private AppRepository appRepository;
    
    @Mock
    private UserRatingRepository userRatingRepository;

    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private RecommendationService recommendationService;
    
    private App app1, app2, app3, app4, app5;
    
    @BeforeEach
    // 在setUp方法中添加更真实的标签数据
    void setUp() {
        // 初始化游戏数据
        app1 = new App();
        app1.setAppId(1L);
        app1.setTags(new HashSet<>(Arrays.asList("动作", "冒险", "单人", "剧情")));
        
        app2 = new App();
        app2.setAppId(2L);
        app2.setTags(new HashSet<>(Arrays.asList("角色扮演", "策略", "多人", "开放世界")));
        
        app3 = new App();
        app3.setAppId(3L);
        app3.setTags(new HashSet<>(Arrays.asList("射击", "多人", "竞技", "PVP")));
        
        app4 = new App();
        app4.setAppId(4L);
        app4.setTags(new HashSet<>(Arrays.asList("体育", "竞技", "模拟", "多人")));
        
        app5 = new App();
        app5.setAppId(5L);
        app5.setTags(new HashSet<>(Arrays.asList("解谜", "休闲", "益智", "单人")));
        
        // 模拟appRepository.findAll()返回所有游戏
        when(appRepository.findAll()).thenReturn(Arrays.asList(app1, app2, app3, app4, app5));
        
        // 添加用户偏好标签
        User testUser = new User();
        testUser.setUserId(1L);
        testUser.setTags("动作,射击,竞技");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    }

    // ========== 内容推荐测试 ==========
    @Test
    void testContentBasedRecommendation() {
        // 模拟用户评分数据
        UserRating rating1 = new UserRating(null, 1L, 1L, true, null, System.currentTimeMillis());
        UserRating rating2 = new UserRating(null, 1L, 2L, true, null, System.currentTimeMillis());
        
        when(userRatingRepository.findByUserId(1L)).thenReturn(Arrays.asList(rating1, rating2));
        
        List<Long> result = recommendationService.recommendGames(1L, 3);
        
        // 验证结果包含用户偏好的游戏类型
        assertTrue(result.contains(1L)); // 动作游戏
        assertTrue(result.contains(2L)); // 角色扮演游戏
    }

    // ========== 协同过滤推荐测试 ==========
    @Test
    void testCollaborativeFiltering() {
        // 模拟相似用户评分
        UserRating similarRating = new UserRating(null, 2L, 3L, true, null, System.currentTimeMillis());
        
        when(userRatingRepository.findByUserId(1L)).thenReturn(Arrays.asList(
            new UserRating(null, 1L, 1L, true, null, System.currentTimeMillis()),
            new UserRating(null, 1L, 2L, true, null, System.currentTimeMillis())
        ));
        when(userRatingRepository.findSimilarUserRatings(1L)).thenReturn(Arrays.asList(
            similarRating
        ));
        
        List<Long> result = recommendationService.recommendGames(1L, 3);
        
        // 验证协同过滤结果
        assertTrue(result.contains(3L)); // 相似用户喜欢的射击游戏
    }

    // ========== 冷启动测试 ==========
    @Test
    void testColdStartRecommendation() {
        // 冷启动用户(评分少于5个)
        when(userRatingRepository.findByUserId(1L)).thenReturn(Collections.singletonList(
            new UserRating(null, 1L, 1L, true, null, System.currentTimeMillis())
        ));
        
        List<Long> result = recommendationService.recommendGames(1L, 3);
        
        // 冷启动时应该返回热门游戏
        assertFalse(result.isEmpty());
    }

    // ========== 混合推荐测试 ==========
    @Test
    void testHybridRecommendation() {
        // 设置用户评分和相似用户评分
        when(userRatingRepository.findByUserId(1L)).thenReturn(Arrays.asList(
            new UserRating(null, 1L, 1L, true, null, System.currentTimeMillis()),
            new UserRating(null, 1L, 2L, true, null, System.currentTimeMillis())
        ));
        when(userRatingRepository.findSimilarUserRatings(1L)).thenReturn(Arrays.asList(
            new UserRating(null, 2L, 3L, true, null, System.currentTimeMillis()),
            new UserRating(null, 2L, 4L, true, null, System.currentTimeMillis())
        ));
        
        List<Long> result = recommendationService.recommendGames(1L, 5);
        
        // 验证混合推荐结果
        assertEquals(5, result.size());
        assertTrue(result.contains(1L)); // 内容匹配
        assertTrue(result.contains(3L)); // 协同过滤
    }

    // ========== 权重调整测试 ==========
    @Test
    void testWeightAdjustmentForColdStart() {
        // 冷启动用户(评分少于5个)
        when(userRatingRepository.findByUserId(1L)).thenReturn(Arrays.asList(
            new UserRating(null, 1L, 1L, true, null, System.currentTimeMillis())
        ));
        
        List<Long> result = recommendationService.recommendGames(1L, 3);
        
        // 冷启动时内容权重应该更高
        assertEquals(1L, result.get(0)); // 内容匹配优先
    }

}
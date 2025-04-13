package com.wbbb.steam.service;

import com.wbbb.steam.config.SQLStatisticsInspector;
import com.wbbb.steam.entity.App;
import com.wbbb.steam.entity.User;
import com.wbbb.steam.entity.UserRating;
import com.wbbb.steam.repository.AppRepository;
import com.wbbb.steam.repository.UserRatingRepository;
import com.wbbb.steam.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Timeout;

@SpringBootTest(properties = "spring.profiles.active=perf")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RecommendationServicePerfTest {

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
    void setUp() {
        // 初始化测试数据
        app1 = new App();
        app1.setAppId(1L);
        app1.setTags(new HashSet<>(Arrays.asList("动作", "冒险")));
        
        app2 = new App();
        app2.setAppId(2L);
        app2.setTags(new HashSet<>(Arrays.asList("角色扮演", "策略")));
        
        app3 = new App();
        app3.setAppId(3L);
        app3.setTags(new HashSet<>(Arrays.asList("射击", "多人")));
        
        app4 = new App();
        app4.setAppId(4L);
        app4.setTags(new HashSet<>(Arrays.asList("体育", "竞技")));
        
        app5 = new App();
        app5.setAppId(5L);
        app5.setTags(new HashSet<>(Arrays.asList("解谜", "休闲")));

        // 模拟用户偏好
        User testUser = new User();
        testUser.setUserId(1L);
        testUser.setTags("动作,射击");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // 基础数据模拟
        when(appRepository.findAll()).thenReturn(Arrays.asList(app1, app2, app3, app4, app5));
    }

    @RepeatedTest(10)
    @Timeout(5) // 单位：秒
    void testRecommendationPerformance() {
        // 生成1000条测试数据
        List<UserRating> mockRatings = new ArrayList<>();
        Random rand = new Random();
        for (long i = 1; i <= 1000; i++) {
            mockRatings.add(new UserRating(
                null, 
                1L, 
                i, 
                rand.nextDouble() > 0.5, // 随机推荐
                null,
                System.currentTimeMillis()
            ));
        }
        when(userRatingRepository.findByUserId(1L)).thenReturn(mockRatings);

        long startTime = System.currentTimeMillis();
        List<Long> result = recommendationService.recommendGames(1L, 10);
        long duration = System.currentTimeMillis() - startTime;
        
        assertFalse(result.isEmpty());
        System.out.printf("[性能测试] 推荐计算耗时: %d ms%n", duration);
    }

    @Test
    void testConcurrentRecommendations() throws Exception {
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 并发请求
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    List<Long> result = recommendationService.recommendGames(1L, 10);
                    assertFalse(result.isEmpty());
                } finally {
                    latch.countDown();
                }
            });
        }

        // 验证并发处理能力
        assertTrue(latch.await(5, TimeUnit.SECONDS), "并发请求未在5秒内完成");
        executor.shutdownNow();
    }

    @Test
    void testQueryOptimization() {
        SQLStatisticsInspector.reset();
        
        // 执行推荐计算
        recommendationService.recommendGames(1L, 10);
        
        // 验证查询性能
        long avgTime = SQLStatisticsInspector.getAverageTime();
        System.out.printf("[SQL性能] 平均查询时间: %d ms%n", avgTime);
        assertTrue(avgTime < 50, "SQL平均查询时间超标，当前值: " + avgTime + "ms");
        
        // 验证查询次数
        verify(appRepository, times(1)).findAll();
        verify(userRatingRepository, times(1)).findByUserId(anyLong());
    }
}
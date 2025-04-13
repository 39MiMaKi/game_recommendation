package com.wbbb.steam.config;

import com.wbbb.steam.entity.RecommendationConfig;
import com.wbbb.steam.repository.RecommendationConfigRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 推荐系统配置Bean
 * 用于提供RecommendationConfig实例作为Spring Bean
 */
@Configuration
public class RecommendationConfigBean {

    /**
     * 创建RecommendationConfig Bean
     * 如果数据库中存在配置，则使用数据库中的配置
     * 否则创建默认配置
     *
     * @param repository RecommendationConfig仓库接口
     * @return RecommendationConfig实例
     */
    @Bean
    public RecommendationConfig recommendationConfig(RecommendationConfigRepository repository) {
        // 尝试从数据库获取配置，如果没有则创建默认配置
        return repository.findAll().stream().findFirst().orElseGet(() -> {
            // 创建默认配置
            RecommendationConfig config = new RecommendationConfig();
            config.setContentWeight(0.6);
            config.setCollaborativeWeight(0.4);
            config.setSimilarityThreshold(0.3);
            config.setAlgorithmVersion("v1.0");
            config.setCollaborativeFilteringWeight(0.5);
            config.setContentBasedWeight(0.5);
            config.setHybridWeight(0.6);
            config.setCreateTime(System.currentTimeMillis());
            
            // 保存到数据库
            return repository.save(config);
        });
    }
}
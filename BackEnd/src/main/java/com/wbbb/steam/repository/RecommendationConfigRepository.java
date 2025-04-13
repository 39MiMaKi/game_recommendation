package com.wbbb.steam.repository;

import com.wbbb.steam.entity.RecommendationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface RecommendationConfigRepository extends JpaRepository<RecommendationConfig, Long> {
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO recommendation_config (algorithm_version, weight) VALUES ('content_based', 0.6), ('collaborative', 0.4) ON CONFLICT (algorithm_version) DO NOTHING", 
           nativeQuery = true)
    void initRecommendationConfig();
}
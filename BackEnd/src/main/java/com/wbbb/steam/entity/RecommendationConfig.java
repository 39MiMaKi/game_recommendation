package com.wbbb.steam.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "recommendation_config")
public class RecommendationConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_weight", nullable = false)
    private Double contentWeight;

    @Column(name = "collaborative_weight", nullable = false)
    private Double collaborativeWeight;

    @Column(name = "similarity_threshold", nullable = false)
    private Double similarityThreshold;

    @Column(name = "algorithm_version", nullable = false)
    private String algorithmVersion;

    @Column(name = "collaborative_filtering_weight", nullable = false, columnDefinition = "DOUBLE DEFAULT 0.5")
    private Double collaborativeFilteringWeight;

    @Column(name = "content_based_weight", nullable = false, columnDefinition = "DOUBLE DEFAULT 0.5")
    private Double contentBasedWeight;

    @Column(name = "hybrid_weight", nullable = false, columnDefinition = "DOUBLE DEFAULT 0.6")
    private Double hybridWeight;
    @Column(name = "create_time", nullable = false)
    private Long createTime;

    @Builder
    public RecommendationConfig(Long id, Double collaborativeFilteringWeight, Double contentBasedWeight) {
        this.id = id;
        this.collaborativeFilteringWeight = collaborativeFilteringWeight != null ? collaborativeFilteringWeight : 0.5;
        this.contentBasedWeight = contentBasedWeight != null ? contentBasedWeight : 0.5;
    }
}
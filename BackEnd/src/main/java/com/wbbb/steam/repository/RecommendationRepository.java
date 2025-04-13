package com.wbbb.steam.repository;

import java.util.Map;

public interface RecommendationRepository {
    /**
     * 获取完整的用户-游戏评分矩阵
     */
    Map<Long, Map<Long, Double>> getUserGameRatingMatrix();

    /**
     * 获取标签聚合统计信息
     * @param tag 标签名称
     * @return 包含该标签的游戏数量和平均评分
     */
    Map<String, Object> getTagAggregationStats(String tag);

    /**
     * 获取用户相似度矩阵
     * @param threshold 最小共同评分数量阈值
     */
    Map<Long, Map<Long, Double>> getUserSimilarityMatrix(int threshold);

    /**
     * 获取热门标签列表
     * @param limit 返回数量限制
     */
    Map<String, Long> getPopularTags(int limit);
}
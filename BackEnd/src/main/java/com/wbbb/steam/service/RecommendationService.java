// RecommendationService.java
package com.wbbb.steam.service;

import com.wbbb.steam.entity.App;
import com.wbbb.steam.entity.RecommendationConfig;
import com.wbbb.steam.entity.User;
import com.wbbb.steam.entity.UserRating;
import com.wbbb.steam.repository.AppRepository;
import com.wbbb.steam.repository.UserRatingRepository;
import com.wbbb.steam.repository.UserRepository;
import com.wbbb.steam.repository.UserRatingRepository.UserRatingProjection;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐系统服务类
 * 提供基于内容和协同过滤的混合推荐算法
 */
@AllArgsConstructor
@Service
public class RecommendationService {
   
    
    
  // ====================== 依赖注入 ======================
  private final UserRatingRepository userRatingRepository;  
  private final AppRepository appRepository;
  private final UserRepository userRepository;
  private RecommendationConfig recommendationConfig;

    // ====================== 系统常量 ======================
    /** 内容推荐权重(默认0.6) */
    private static final double CONTENT_WEIGHT = 0.6;
    /** 协同过滤权重(默认0.4) */
    private static final double COLLABORATIVE_WEIGHT = 0.4;
    /** 冷启动阈值(评分少于5个视为新用户) */
    private static final int COLD_START_THRESHOLD = 5;
    /** 流行度衰减系数 */
    private static final double POPULARITY_DECAY = 0.95;

  

    // ====================== 核心推荐方法 ======================

    /**
     * 混合推荐主方法
     * @param userId 用户ID
     * @param limit 返回推荐数量
     * @return 推荐游戏ID列表，按推荐分数降序排列
     */
    public List<Long> recommendGames(Long userId, int limit) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<App> allGames = appRepository.findAll();
        List<UserRating> userRatings = userRatingRepository.findByUserId(userId);
        boolean isColdStart = userRatings.size() < COLD_START_THRESHOLD;
        
        Map<Long, Double> contentScores = contentBasedFiltering(userId, allGames);
        Map<Long, Double> collaborativeScores = collaborativeFiltering(userId);
        
        Map<Long, Double> finalScores = new HashMap<>();
        double contentWeightAdjusted = isColdStart ? 0.9 : CONTENT_WEIGHT;
        double collaborativeWeightAdjusted = isColdStart ? 0.1 : COLLABORATIVE_WEIGHT;
        
        allGames.forEach(game -> {
            double score = contentWeightAdjusted * contentScores.getOrDefault(game.getAppId(), 0.0)
                    + collaborativeWeightAdjusted * collaborativeScores.getOrDefault(game.getAppId(), 0.0);
            finalScores.put(game.getAppId(), score);
        });
        
        return finalScores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // ====================== 内容推荐模块 ======================

    /**
     * 基于内容的推荐算法
     * @param userId 用户ID
     * @param allGames 所有游戏列表
     * @return 游戏ID到内容推荐分数的映射
     */
    private Map<Long, Double> contentBasedFiltering(Long userId, List<App> allGames) {
        Map<String, Double> userVector = getUserPreferenceVector(userId);
        if (userVector.isEmpty()) {
            return getColdStartRecommendations(allGames, userId);
        }
        
        Map<Long, Double> gameScores = new HashMap<>();
        Set<String> userTags = getUserPreferenceTags(userId);
        
        allGames.forEach(game -> {
            if (game.getTags() == null || game.getTags().isEmpty()) {
                gameScores.put(game.getAppId(), 0.0);
                return;
            }
            
            Set<String> gameTags = new HashSet<>(game.getTags());
            gameTags.retainAll(userTags);
            double similarity = (double) gameTags.size() / (userTags.size() + game.getTags().size() - gameTags.size());
            double popularityScore = game.getPopularity() != null ? 
                game.getPopularity().doubleValue() * Math.pow(POPULARITY_DECAY, allGames.indexOf(game)) : 0;
            
            gameScores.put(game.getAppId(), 0.7 * similarity + 0.3 * popularityScore);
        });
        
        return gameScores;
    }

    /**
     * 获取用户偏好向量
     * @param userId 用户ID
     * @return 标签到权重的映射(归一化到0-1)
     */
    private Map<String, Double> getUserPreferenceVector(Long userId) {
        Map<String, Double> vector = new HashMap<>();
        List<UserRating> ratings = userRatingRepository.findByUserId(userId);
        if (ratings.isEmpty()) return vector;
    
        ratings.forEach(rating -> {
            App app = appRepository.findById(rating.getAppId()).orElse(null);
            if (app != null && app.getTags() != null) {
                // 将推荐状态转换为评分值：推荐=1.0，不推荐=0.0
                double ratingValue = rating.getRecommended() ? 1.0 : 0.0;
                app.getTags().forEach(tag -> {
                    vector.put(tag, vector.getOrDefault(tag, 0.0) + ratingValue);
                });
            }
        });
    
        double maxWeight = vector.values().stream().max(Double::compare).orElse(1.0);
        vector.replaceAll((k, v) -> v / maxWeight);
        return vector;
    }

    /**
     * 冷启动推荐(新用户或无评分用户)
     * @param allGames 所有游戏列表
     * @param userId 用户ID
     * @return 游戏ID到冷启动推荐分数的映射
     */
    private Map<Long, Double> getColdStartRecommendations(List<App> allGames, Long userId) {
        Map<Long, Double> scores = new HashMap<>();
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isPresent() && userOpt.get().getTags() != null && !userOpt.get().getTags().isEmpty()) {
            Set<String> tagsSet = Arrays.stream(userOpt.get().getTags().split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
            allGames.forEach(game -> {
                Set<String> gameTags = game.getTags();
                double tagMatchScore = tagsSet.stream()
                    .filter(gameTags::contains)
                    .count() / (double) tagsSet.size();
                scores.put(game.getAppId(), tagMatchScore);
            });
        } else {
            Page<App> hotGames = appRepository.findTop100ByOrderByPopularityDesc(PageRequest.of(0, 100));
            hotGames.getContent().forEach(game -> 
                scores.put(game.getAppId(), game.getPopularity() != null ? game.getPopularity().doubleValue() : 0.0));
        }
        
        Random rand = new Random();
        scores.replaceAll((k, v) -> v * 0.8 + rand.nextDouble() * 0.2);
        return scores;
    }

    // ====================== 协同过滤模块 ======================

    /**
     * 基于用户的协同过滤算法
     * @param userId 目标用户ID
     * @return 游戏ID到协同过滤推荐分数的映射
     */
    private Map<Long, Double> collaborativeFiltering(Long userId) {
        Map<Long, Double> gameScores = new HashMap<>();
        List<UserRating> userRatings = userRatingRepository.findByUserId(userId);
        if (userRatings.isEmpty()) return gameScores;
        
        Map<Long, Double> userSimilarities = new HashMap<>();
        Map<Long, Map<Long, Double>> userRatingMap = getAllRatings(); // 改为调用本地方法
        Map<Long, Double> currentUserRatings = userRatingMap.getOrDefault(userId, Collections.emptyMap());
        
        for (Map.Entry<Long, Map<Long, Double>> entry : userRatingMap.entrySet()) {
            Long otherUserId = entry.getKey();
            if (otherUserId.equals(userId)) continue;
            
            Map<Long, Double> otherUserRatings = entry.getValue();
            double similarity = calculateRatingSimilarity(currentUserRatings, otherUserRatings);
            if (similarity > 0) {
                userSimilarities.put(otherUserId, similarity);
            }
        }
        
        Set<Long> candidateGames = new HashSet<>();
        for (Long similarUserId : userSimilarities.keySet()) {
            candidateGames.addAll(userRatingMap.get(similarUserId).keySet());
        }
        candidateGames.removeAll(currentUserRatings.keySet());
        
        for (Long gameId : candidateGames) {
            double scoreSum = 0;
            double similaritySum = 0;
            
            for (Map.Entry<Long, Double> entry : userSimilarities.entrySet()) {
                Long similarUserId = entry.getKey();
                double similarity = entry.getValue();
                Map<Long, Double> similarUserRatings = userRatingMap.getOrDefault(similarUserId, Collections.emptyMap());
                if (similarUserRatings.containsKey(gameId)) {
                    scoreSum += similarity * similarUserRatings.get(gameId);
                    similaritySum += similarity;
                }
            }
            
            if (similaritySum > 0) {
                gameScores.put(gameId, scoreSum / similaritySum);
            }
        }
        
        return gameScores;
    }

    // ====================== 相似度计算 ======================
    private Map<Long, Map<Long, Double>> getAllRatings() {
        return userRatingRepository.getAllRatingsProjection().stream()
            .collect(Collectors.groupingBy(
                UserRatingProjection::getUserId,
                Collectors.toMap(
                    UserRatingProjection::getAppId,
                    projection -> projection.getRecommended() ? 1.0 : 0.0
                )
            ));
    }
    
    /**
     * 计算评分相似度(皮尔逊相关系数)
     * @param v1 用户1的评分向量
     * @param v2 用户2的评分向量
     * @return 相似度分数
     */
    private double calculateRatingSimilarity(Map<Long, Double> v1, Map<Long, Double> v2) {
        double dotProduct = 0;
        double norm1 = 0;
        double norm2 = 0;
        Set<Long> common = new HashSet<>(v1.keySet());
        common.retainAll(v2.keySet());
        
        if (common.isEmpty()) return 0;
        
        for (Long key : common) {
            dotProduct += v1.get(key) * v2.get(key);
        }
        for (Double value : v1.values()) norm1 += value * value;
        for (Double value : v2.values()) norm2 += value * value;
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    // ====================== 标签管理 ======================

    /**
     * 更新用户偏好标签
     * @param userId 用户ID
     * @param tags 标签字符串(逗号分隔)
     * @return 是否更新成功
     */
    public boolean updateUserPreferenceTags(Long userId, String tags) {
        if (tags == null || tags.isEmpty()) return false;
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) return false;
        
        User user = userOpt.get();
        user.setTags(tags);
        userRepository.save(user);
        return true;
    }

    /**
     * 获取用户偏好标签集合
     * @param userId 用户ID
     * @return 用户偏好标签集合
     */
    private Set<String> getUserPreferenceTags(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent() && userOpt.get().getTags() != null) {
            return Arrays.stream(userOpt.get().getTags().split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        }
        
        List<UserRating> ratings = userRatingRepository.findByUserId(userId);
        if (ratings.isEmpty()) return Collections.emptySet();
        
        List<Long> highRatedGameIds = ratings.stream()
                .filter(UserRating::getRecommended) // 只选择用户推荐的游戏
                .map(UserRating::getAppId)
                .collect(Collectors.toList());
        
        Set<String> extractedTags = new HashSet<>();
        highRatedGameIds.forEach(gameId -> {
            appRepository.findById(gameId).ifPresent(game -> {
                if (game.getTags() != null) extractedTags.addAll(game.getTags());
            });
        });
        
        return extractedTags;
    }

    // ====================== 系统配置 ======================

    /**
     * 获取推荐系统参数配置
     * @return 包含权重参数的Map
     */
    public Map<String, Double> getRecommendationParameters() {
        return Map.of(
            "contentWeight", CONTENT_WEIGHT,
            "collaborativeWeight", COLLABORATIVE_WEIGHT,
            "coldStartThreshold", (double) COLD_START_THRESHOLD,
            "popularityDecay", POPULARITY_DECAY
        );
    }

    public boolean updateRecommendationParameters(Map<String, Double> parameters) {
        try {
            // 实际项目应在此处更新配置存储
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public RecommendationConfig getConfig() {
        return recommendationConfig;
    }

    public RecommendationConfig updateConfig(RecommendationConfig config) {
        this.recommendationConfig = config;
        return recommendationConfig;
    }

    public RecommendationConfig updateRecommendationConfig(RecommendationConfig config) {
        this.recommendationConfig = config;
        return recommendationConfig;
    }

    // 添加在getRecommendationParameters方法附近
    /** 获取内容推荐权重 */
    public double getContentWeight() {
        return CONTENT_WEIGHT;
    }
    
    /** 获取协同过滤权重 */
    public double getCollaborativeWeight() {
        return COLLABORATIVE_WEIGHT;
    }
    
    /** 获取冷启动阈值 */
    public int getColdStartThreshold() {
        return COLD_START_THRESHOLD;
    }
    
    /** 获取流行度衰减系数 */
    public double getPopularityDecay() {
        return POPULARITY_DECAY;
    }
    
    /**
     * 更新用户偏好
     * @param userId 用户ID
     * @param gameTags 游戏标签集合
     * @param score 评分
     */
    public void updateUserPreferences(Long userId, Set<String> gameTags, Double score) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return;
        }
        
        User user = userOpt.get();
        String currentTags = user.getTags();
        String newTags = String.join(",", gameTags);
        
        // 根据评分权重合并标签
        if (currentTags == null || currentTags.isEmpty()) {
            user.setTags(newTags);
        } else {
            int repeat = Math.min(5, (int) Math.round(score));
            user.setTags(currentTags + "," + String.join(",", Collections.nCopies(repeat, newTags)));
        }
        
        userRepository.save(user);
    }
}
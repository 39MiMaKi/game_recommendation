package com.wbbb.steam.repository;

import com.wbbb.steam.dto.response.data.AppDto;
import com.wbbb.steam.entity.App;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface AppRepository extends JpaRepository<App, Long> {

    /** 获取推荐列表 */
    @Query("SELECT new com.wbbb.steam.dto.response.data.AppDto(a, CASE WHEN w IS NOT NULL THEN 1 ELSE 0 END) FROM App a " +
            "LEFT JOIN WishlistItem w ON a.appId = w.appId AND w.userId = :userId")
    Page<AppDto> getRecommendations(Pageable pageable, Long userId);

    /** 获取游戏详情 */
    @Query("SELECT new com.wbbb.steam.dto.response.data.AppDto(a, CASE WHEN w IS NOT NULL THEN 1 ELSE 0 END) FROM App a " +
            "LEFT JOIN WishlistItem w ON a.appId = w.appId AND w.userId = :userId " +
            "WHERE a.appId = :appId")
    AppDto getApp(Long appId, Long userId);

    /** 搜索游戏 */
    @Query("SELECT new com.wbbb.steam.dto.response.data.AppDto(a, CASE WHEN w IS NOT NULL THEN 1 ELSE 0 END) FROM App a " +
            "LEFT JOIN WishlistItem w ON a.appId = w.appId AND w.userId = :userId " +
            "WHERE LOWER(a.name) LIKE %:keyword% " +
            "OR LOWER(a.developer) LIKE %:keyword% " +
            "OR LOWER(a.publisher) LIKE %:keyword%")
    List<AppDto> searchApp(String keyword, Long userId);

    /** 获取按人气降序排列的前100个应用 */
    Page<App> findTop100ByOrderByPopularityDesc(Pageable pageable);
    
    /** 获取所有游戏（管理员后台使用） */
    @Query("SELECT new com.wbbb.steam.dto.response.data.AppDto(a, 0) FROM App a")
    Page<AppDto> findAllApps(Pageable pageable);
    
    /** 根据标签查询游戏 */
    @Query("SELECT a FROM App a WHERE :tag MEMBER OF a.tags")
    List<App> findByTag(String tag);
    
    // 修改所有rating相关查询方法为positiveRate
    Page<App> findByPositiveRateBetween(Double minRating, Double maxRating, Pageable pageable);
    Page<App> findByPositiveRateGreaterThanEqual(Double minRating, Pageable pageable);
    Page<App> findByPositiveRateLessThanEqual(Double maxRating, Pageable pageable);

    /** 根据关键词搜索游戏(分页) */
    @Query("SELECT a FROM App a WHERE LOWER(a.name) LIKE %:keyword% " +
           "OR LOWER(a.developer) LIKE %:keyword% " +
           "OR LOWER(a.publisher) LIKE %:keyword%")
    Page<App> search(String keyword, Pageable pageable);
    
    /** 根据标签查询游戏(分页) */
    @Query("SELECT a FROM App a WHERE :tag MEMBER OF a.tags")
    Page<App> findByTagsContaining(String tag, Pageable pageable);
    
    /**
     * 多维度筛选游戏
     * @param tags 标签列表
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param startDate 开始日期（格式为"yyyy-MM-dd"的字符串）
     * @param endDate 结束日期（格式为"yyyy-MM-dd"的字符串）
     * @param pageable 分页参数
     * @param userId 用户ID（用于判断是否在愿望单中）
     * @return 游戏列表
     */
    @Query("SELECT new com.wbbb.steam.dto.response.data.AppDto(a, CASE WHEN w IS NOT NULL THEN 1 ELSE 0 END) FROM App a " +
            "LEFT JOIN WishlistItem w ON a.appId = w.appId AND w.userId = :userId " +
            "WHERE (:tags IS NULL OR EXISTS (SELECT t FROM a.tags t WHERE t IN :tags)) " +
            "AND (:minPrice IS NULL OR a.finalPrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR a.finalPrice <= :maxPrice) " +
            "AND (:startDate IS NULL OR a.createTime >= :startDate) " +
            "AND (:endDate IS NULL OR a.createTime <= :endDate)")
    Page<AppDto> findByMultipleFilters(
            @Param("tags") Set<String> tags,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("userId") Long userId,
            Pageable pageable);
    
    /**
     * 按标签筛选游戏
     */
    @Query("SELECT new com.wbbb.steam.dto.response.data.AppDto(a, CASE WHEN w IS NOT NULL THEN 1 ELSE 0 END) FROM App a " +
            "LEFT JOIN WishlistItem w ON a.appId = w.appId AND w.userId = :userId " +
            "WHERE :tag MEMBER OF a.tags")
    Page<AppDto> findByTag(@Param("tag") String tag, @Param("userId") Long userId, Pageable pageable);
    
    /**
     * 按价格区间筛选游戏
     */
    @Query("SELECT new com.wbbb.steam.dto.response.data.AppDto(a, CASE WHEN w IS NOT NULL THEN 1 ELSE 0 END) FROM App a " +
            "LEFT JOIN WishlistItem w ON a.appId = w.appId AND w.userId = :userId " +
            "WHERE a.finalPrice BETWEEN :minPrice AND :maxPrice")
    Page<AppDto> findByPriceRange(
            @Param("minPrice") Double minPrice, 
            @Param("maxPrice") Double maxPrice, 
            @Param("userId") Long userId, 
            Pageable pageable);
    
    /**
     * 按发行日期区间筛选游戏
     */
    @Query("SELECT new com.wbbb.steam.dto.response.data.AppDto(a, CASE WHEN w IS NOT NULL THEN 1 ELSE 0 END) FROM App a " +
            "LEFT JOIN WishlistItem w ON a.appId = w.appId AND w.userId = :userId " +
            "WHERE a.createTime BETWEEN :startDate AND :endDate")
    Page<AppDto> findByDateRange(
            @Param("startDate") String startDate, 
            @Param("endDate") String endDate, 
            @Param("userId") Long userId, 
            Pageable pageable);
}

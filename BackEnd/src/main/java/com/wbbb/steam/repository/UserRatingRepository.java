package com.wbbb.steam.repository;

import com.wbbb.steam.entity.UserRating;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRatingRepository extends JpaRepository<UserRating, Long> {
    Optional<UserRating> findByUserIdAndAppId(Long userId, Long appId);
    
    List<UserRating> findByUserId(Long userId);
    
  
    
    List<UserRating> findByAppId(Long appId);
    
    Page<UserRating> findByAppId(Long appId, Pageable pageable);
    
    // 修改为使用接口投影
    @Query("SELECT ur.userId as userId, ur.appId as appId, ur.recommended as recommended FROM UserRating ur")
    List<UserRatingProjection> getAllRatingsProjection();
    
    interface UserRatingProjection {
        Long getUserId();
        Long getAppId();
        Boolean getRecommended();
    }
    
    @Query("SELECT ur FROM UserRating ur WHERE ur.userId = :userId AND ur.appId = :appId")
    Optional<UserRating> findUserRating(Long userId, Long appId);
    
    @Query("SELECT COUNT(ur) FROM UserRating ur WHERE ur.appId = :appId AND ur.recommended = true")
    Long countPositiveRatingsByAppId(Long appId);
    
    @Query("SELECT COUNT(ur) FROM UserRating ur WHERE ur.appId = :appId")
    Long countTotalRatingsByAppId(Long appId);
    
    Integer countByUserId(Long userId);
    
    @Query("SELECT COUNT(ur) FROM UserRating ur WHERE ur.userId = :userId AND ur.recommended = true")
    Long countPositiveRatingsByUserId(@Param("userId") Long userId);
    
    /**
     * 计算用户的推荐率（好评率）
     * @param userId 用户ID
     * @return 推荐率（0-100的百分比值）
     */
    @Query("SELECT CASE WHEN COUNT(ur) > 0 THEN (SUM(CASE WHEN ur.recommended = true THEN 1 ELSE 0 END) * 100.0 / COUNT(ur)) ELSE 0 END FROM UserRating ur WHERE ur.userId = :userId")
    Double calculatePositiveRateByUserId(@Param("userId") Long userId);
    
    Page<UserRating> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
    
    Page<UserRating> findAllByOrderByTimestampDesc(Pageable pageable);
    
    @Query("SELECT ur FROM UserRating ur WHERE ur.userId IN " +
           "(SELECT similar.userId FROM UserRating similar " +
           "WHERE similar.appId IN (SELECT r.appId FROM UserRating r WHERE r.userId = :userId) " +
           "GROUP BY similar.userId " +
           "ORDER BY COUNT(similar.appId) DESC)")
    List<UserRating> findSimilarUserRatings(@Param("userId") Long userId);
}
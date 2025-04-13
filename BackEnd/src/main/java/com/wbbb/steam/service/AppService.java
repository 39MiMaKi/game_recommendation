// AppService.java
package com.wbbb.steam.service;

import com.wbbb.steam.dto.request.AppFilterDto;
import com.wbbb.steam.dto.response.PageDto;
import com.wbbb.steam.dto.response.data.AppDto;
import com.wbbb.steam.entity.App;
import com.wbbb.steam.repository.AppRepository;
import com.wbbb.steam.util.TokenUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class AppService {
    
    // ====================== 依赖注入 ======================
    private final AppRepository appRepository;
    private final RecommendationService recommendationService;

 
    
    /**
     * 获取游戏详情
     * @param appId 游戏ID
     * @param token 用户令牌 
     * @return 返回游戏详情DTO，包含用户个性化数据
     */
    public AppDto getApp(Long appId, String token) {
        Long userId = TokenUtil.parseToken(token);
        return appRepository.getApp(appId, userId);
    }

     
    /**
     * 分页获取游戏列表
     * @param page 页码
     * @param size 每页数量
     * @param keyword 搜索关键词
     * @param tag 标签筛选
     * @return 分页结果DTO
     */
    public PageDto<AppDto> getApps(int page, int size, String keyword, String tag) {
        Pageable pageable = PageRequest.of(page, size);
        Page<App> appPage;
        
        try {
            if (keyword != null && !keyword.isEmpty()) {
                appPage = appRepository.search("%" + keyword.toLowerCase() + "%", pageable);
            } else if (tag != null && !tag.isEmpty()) {
                appPage = appRepository.findByTagsContaining(tag, pageable);
            } else {
                appPage = appRepository.findAll(pageable);
            }
            
            List<AppDto> content = appPage.getContent().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
                    
            return new PageDto<>(
                content,
                appPage.getNumber(),
                appPage.getSize(),
                appPage.getTotalElements(),
                appPage.getTotalPages()
            );
        } catch (Exception e) {
            return new PageDto<>(
                Collections.emptyList(),
                page,
                size,
                0,
                0
            );
        }
    }
    /**
     * 获取搜索建议
     * @param keyword 搜索关键词
     * @param num 返回结果数量
     * @param token 用户令牌
     * @return 返回匹配的游戏列表，按相关性排序
     */
    public List<AppDto> getAppSearchSuggestions(String keyword, Integer num, String token) {
        List<String> words = List.of(keyword.toLowerCase().split("\s+"));
        List<AppDto> appList = new ArrayList<>();
        Long userId = TokenUtil.parseToken(token);
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            List<AppDto> temp = appRepository.searchApp(word, userId);
            if (i == 0)
                appList.addAll(temp);
            else
                appList.retainAll(temp);
        }
        if (appList.size() > num)
            appList.subList(0, num);
        return appList;
    }

    // ====================== 推荐功能 ======================
    
    /**
     * 获取基于时间的游戏推荐（最新游戏）
     * @param num 推荐数量
     * @param token 用户令牌
     * @return 返回按创建时间倒序的最新游戏列表
     */
    public List<AppDto> getAppRecommendations(Integer num, String token) {
        Sort sort = Sort.by("createTime").descending();
        Pageable pageable = PageRequest.of(0, num, sort);
        Long userId = TokenUtil.parseToken(token);
        return appRepository.getRecommendations(pageable, userId).getContent();
    }

    /**
     * 获取混合推荐结果（协同过滤+内容推荐）
     * @param token 用户令牌
     * @return 返回推荐游戏列表
     */
    public List<AppDto> getRecommendations(String token) {
        Long userId = token != null ? TokenUtil.parseToken(token) : null;
        List<Long> recommendedAppIds = recommendationService.recommendGames(userId, 12);
        return recommendedAppIds != null && !recommendedAppIds.isEmpty() 
            ? convertToDtos(recommendedAppIds) 
            : getAppRecommendations(12, token);
    }
    
    /**
     * 获取个性化推荐游戏
     * @param token 用户令牌
     * @param limit 返回数量限制
     * @return 返回个性化推荐游戏列表
     */
    public List<AppDto> getPersonalizedRecommendations(String token, int limit) {
        Long userId = TokenUtil.parseToken(token);
        List<Long> recommendedAppIds = recommendationService.recommendGames(userId, limit);
        return convertToDtos(recommendedAppIds);
    }
    
    // ====================== 分页查询 ======================
    
    /**
     * 按评分范围分页查询游戏
     * @param page 页码
     * @param size 每页数量
     * @param minRating 最低评分
     * @param maxRating 最高评分
     * @param sortDirection 排序方向
     * @return 返回分页结果DTO
     */
    public PageDto<AppDto> getAppsByRating(int page, int size, Double minRating, Double maxRating, Sort.Direction sortDirection) {
        Pageable pageable = PageRequest.of(page, size);
        Page<App> appPage = getFilteredAppPage(minRating, maxRating, pageable);
        
        List<AppDto> content = appPage.getContent()
            .stream()
            .sorted((a1, a2) -> sortDirection == Sort.Direction.DESC 
                ? Double.compare(a2.getPositiveRate(), a1.getPositiveRate())
                : Double.compare(a1.getPositiveRate(), a2.getPositiveRate()))
            .map(this::convertToDto)
            .collect(Collectors.toList());
            
        return new PageDto<>(
            content,
            appPage.getNumber(),
            appPage.getSize(),
            appPage.getTotalElements(),
            appPage.getTotalPages()
        );
    }

    public PageDto<AppDto> getHighRatingApps(int page, int size, double minRating) {
        return getAppsByRating(page, size, minRating, null, Sort.Direction.DESC);
    }

    public PageDto<AppDto> getLowRatingApps(int page, int size, double maxRating) {
        return getAppsByRating(page, size, null, maxRating, Sort.Direction.ASC);
    }

    public PageDto<AppDto> getAppsByRatingRange(int page, int size, Double minRating, Double maxRating) {
        return getAppsByRating(page, size, minRating, maxRating, Sort.Direction.DESC);
    }

    // ====================== 辅助方法 ======================
    
    /**
     * 将游戏ID列表转换为DTO列表
     * @param appIds 游戏ID列表
     * @return 返回游戏DTO列表
     */
    private List<AppDto> convertToDtos(List<Long> appIds) {
        return appIds.stream()
            .map(id -> appRepository.getApp(id, null))
            .collect(Collectors.toList());
    }

    /**
     * 获取过滤后的游戏分页数据
     * @param minRating 最低评分
     * @param maxRating 最高评分
     * @param pageable 分页参数
     * @return 返回游戏分页数据
     */
    private Page<App> getFilteredAppPage(Double minRating, Double maxRating, Pageable pageable) {
        if (minRating != null && maxRating != null) {
            return appRepository.findByPositiveRateBetween(minRating, maxRating, pageable);
        } else if (minRating != null) {
            return appRepository.findByPositiveRateGreaterThanEqual(minRating, pageable);
        } else if (maxRating != null) {
            return appRepository.findByPositiveRateLessThanEqual(maxRating, pageable);
        }
        return appRepository.findAll(pageable);
    }

    /**
     * 将游戏实体转换为DTO
     * @param app 游戏实体
     * @return 返回游戏DTO
     */
    private AppDto convertToDto(App app) {
        if (app == null) return null;
        return AppDto.builder()
            .appId(app.getAppId())
            .name(app.getName())
            .cover(app.getCover())
            .header(app.getHeader())
            .images(app.getImages() != null ? List.of(app.getImages().split(",")) : List.of())
            .description(app.getDescription())
            .developer(app.getDeveloper())
            .publisher(app.getPublisher())
            .price(app.getPrice())
            .finalPrice(app.getFinalPrice())
            .win(app.getWin())
            .mac(app.getMac())
            .linux(app.getLinux())
            .positiveRate(app.getPositiveRate())
            .tags(app.getTags() != null ? new ArrayList<>(app.getTags()) : new ArrayList<>())
            .popularity(app.getPopularity())
            .build();
    }

    // ====================== 推荐系统接口 ======================
    
    /**
     * 获取推荐系统参数配置
     * @return 返回包含权重参数的Map
     */
    public Map<String, Double> getRecommendationParameters() {
        return recommendationService.getRecommendationParameters();
    }

    /**
     * 更新推荐系统参数
     * @param parameters 包含参数键值对的Map
     * @return 返回更新是否成功
     */
    public boolean updateRecommendationParameters(Map<String, Double> parameters) {
        return recommendationService.updateRecommendationParameters(parameters);
    }
    
    /**
     * 更新用户偏好标签
     * @param userId 用户ID
     * @param tags 标签字符串
     * @return 返回更新是否成功
     */
    public boolean updateUserPreferenceTags(Long userId, String tags) {
        return recommendationService.updateUserPreferenceTags(userId, tags);
    }
   

    
    /**
     * 多维度筛选游戏
     * @param token 用户令牌
     * @param filter 筛选条件
     * @param pageIndex 页码
     * @param pageSize 每页数量
     * @param sortBy 排序字段
     * @param sortDir 排序方向
     * @return 游戏列表
     */
    public PageDto<AppDto> filterApps(String token, AppFilterDto filter, 
                                     Integer pageIndex, Integer pageSize, 
                                     String sortBy, String sortDir) {
        Long userId = TokenUtil.parseToken(token);
        
        // 构建排序对象
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(pageIndex, pageSize, sort);
        
        // 调用多维度筛选方法
        Page<AppDto> appPage = appRepository.findByMultipleFilters(
                filter.getTags(),
                filter.getMinPrice(),
                filter.getMaxPrice(),
                filter.getStartDate(),
                filter.getEndDate(),
                userId,
                pageable);
        
        return new PageDto<>(
                appPage.getTotalElements(),
                pageIndex,
                pageSize,
                appPage.getContent()
        );
    }
    
    /**
     * 按标签筛选游戏
     */
    public PageDto<AppDto> getAppsByTag(String token, String tag, 
                                       Integer pageIndex, Integer pageSize, 
                                       String sortBy, String sortDir) {
        Long userId = TokenUtil.parseToken(token);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(pageIndex, pageSize, sort);
        
        Page<AppDto> appPage = appRepository.findByTag(tag, userId, pageable);
        
        return new PageDto<>(
                appPage.getTotalElements(),
                pageIndex,
                pageSize,
                appPage.getContent()
        );
    }
    
    /**
     * 按价格区间筛选游戏
     */
    public PageDto<AppDto> getAppsByPriceRange(String token, Double minPrice, Double maxPrice, 
                                              Integer pageIndex, Integer pageSize, 
                                              String sortBy, String sortDir) {
        Long userId = TokenUtil.parseToken(token);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(pageIndex, pageSize, sort);
        
        Page<AppDto> appPage = appRepository.findByPriceRange(minPrice, maxPrice, userId, pageable);
        
        return new PageDto<>(
                appPage.getTotalElements(),
                pageIndex,
                pageSize,
                appPage.getContent()
        );
    }
    
    /**
     * 按发行日期区间筛选游戏
     */
    public PageDto<AppDto> getAppsByDateRange(String token, String startDate, String endDate, 
                                             Integer pageIndex, Integer pageSize, 
                                             String sortBy, String sortDir) {
        Long userId = TokenUtil.parseToken(token);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(pageIndex, pageSize, sort);
        
        Page<AppDto> appPage = appRepository.findByDateRange(startDate, endDate, userId, pageable);
        
        return new PageDto<>(
                appPage.getTotalElements(),
                pageIndex,
                pageSize,
                appPage.getContent()
        );
    }
}
package com.wbbb.steam.service;

import java.util.HashSet;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.wbbb.steam.dto.request.AppCreateDto;
import com.wbbb.steam.dto.request.AppUpdateDto;
import com.wbbb.steam.dto.response.PageDto;
import com.wbbb.steam.dto.response.data.UserDto;
import com.wbbb.steam.entity.App;
import com.wbbb.steam.entity.User;
import com.wbbb.steam.repository.AppRepository;
import com.wbbb.steam.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminService {

    // ====================== 依赖注入 ======================
    private final AppRepository appRepository;
    private final UserRepository userRepository;
    private final RecommendationService recommendationService;
    
    // ====================== 管理员功能 ======================
    
    /** 
     * 创建新游戏
     * @param dto 包含游戏信息的DTO对象
     * @return 返回新创建游戏的ID
     */
    public Long createApp(AppCreateDto dto) {
        App app = App.builder()
                .name(dto.getName())
                .cover(dto.getCover())
                .header(dto.getHeader())
                .images(dto.getImages())
                .description(dto.getDescription())
                .developer(dto.getDeveloper())
                .publisher(dto.getPublisher())
                .price(dto.getPrice())
                .finalPrice(dto.getFinalPrice())
                .win(dto.getWin())
                .mac(dto.getMac())
                .linux(dto.getLinux())
                .createTime(dto.getCreateTime())
                .tags(dto.getTags() != null ? dto.getTags() : new HashSet<>())
                .popularity(dto.getPopularity())
                .build();
        return appRepository.save(app).getAppId();
    }

    /**
     * 更新游戏信息
     * @param appId 要更新的游戏ID
     * @param dto 包含更新数据的DTO对象
     * @throws IllegalArgumentException 当游戏不存在时抛出
     */
    public void updateApp(Long appId, AppUpdateDto dto) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("游戏不存在"));
        
        app.setName(dto.getName());
        app.setCover(dto.getCover());
        app.setHeader(dto.getHeader());
        app.setImages(dto.getImages());
        app.setDescription(dto.getDescription());
        app.setDeveloper(dto.getDeveloper());
        app.setPublisher(dto.getPublisher());
        app.setPrice(dto.getPrice());
        app.setFinalPrice(dto.getFinalPrice());
        app.setWin(dto.getWin());
        app.setMac(dto.getMac());
        app.setLinux(dto.getLinux());
        app.setTags(dto.getTags());
        app.setPopularity(dto.getPopularity());
        
        appRepository.save(app);
    }

    /**
     * 删除游戏
     * @param appId 要删除的游戏ID
     * @throws IllegalArgumentException 当游戏不存在时抛出
     */
    public void deleteApp(Long appId) {
        if (!appRepository.existsById(appId)) {
            throw new IllegalArgumentException("游戏不存在");
        }
        appRepository.deleteById(appId);
    }

    /**
     * 验证用户是否为管理员
     * @param userId 用户ID
     * @return 是否为管理员
     */
    public boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        User user = getUser(userId);
        return user != null && user.getRole() != null && user.getRole() == 1;
    }

    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户实体
     */
    private User getUser(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * 设置用户角色
     * @param userId 用户ID
     * @param role 角色（0：普通用户，1：管理员）
     * @return 是否设置成功
     */
    public boolean setUserRole(Long userId, Integer role) {
        if (userId == null || (role != 0 && role != 1)) {
            return false;
        }
        User user = getUser(userId);
        if (user == null) {
            return false;
        }
        user.setRole(role);
        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new IllegalArgumentException("更新用户角色失败：" + e.getMessage());
        }
        return true;
    }

    /**
     * 更新用户状态
     * @param userId 用户ID
     * @param enabled 是否启用
     */
    public void updateUserStatus(Long userId, Boolean enabled) {
        User user = getUser(userId);
        if (user != null) {
            user.setEnabled(enabled);
            try {
                userRepository.save(user);
            } catch (Exception e) {
                throw new IllegalArgumentException("更新用户状态失败：" + e.getMessage());
            }
        }
    }

    /**
     * 管理员搜索用户（返回完整信息）
     */
    public PageDto<UserDto> adminSearchUser(String keyword, Integer pageIndex, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageIndex - 1, pageSize);
        Page<User> page = userRepository.findByKeyword(keyword.trim(), pageable);
        
        return new PageDto<>(
            page.getContent().stream()
                .map(user -> new UserDto(
                    user.getUserId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getNickname(),
                    user.getAvatar(),
                    user.getCreateTime(),
                    user.getRole(),
                    user.getEnabled()
                ))
                .toList(),
            page.getNumber() + 1,
            pageSize,
            page.getTotalElements(),
            page.getTotalPages()
        );
    }
    /**
     * 获取推荐系统参数配置
     * @return 返回包含权重参数的Map
     */
    public Map<String, Double> getRecommendationParameters() {
        // 调用推荐服务获取参数
        return recommendationService.getRecommendationParameters();
    }
    
    /**
     * 更新推荐系统参数
     * @param parameters 新的参数配置
     * @return 是否更新成功
     */
    public boolean updateRecommendationParameters(Map<String, Double> parameters) {
        return recommendationService.updateRecommendationParameters(parameters);
    }

    /**
     * 获取用户列表（分页）
     * @param page 页码
     * @param size 每页大小
     * @return 用户分页数据
     */
    public PageDto<User> getUsers(Integer page, Integer size) {
        Page<User> userPage = userRepository.findAll(PageRequest.of(page, size));
        return new PageDto<>(
            userPage.getTotalElements(),
            page,
            size,
            userPage.getContent()
        );
    }

    /**
     * 搜索用户
     * @param page 页码
     * @param size 每页大小
     * @param username 用户名（可选）
     * @param email 邮箱（可选）
     * @return 符合条件的用户分页数据
     */
    public PageDto<User> searchUsers(int page, int size, String username, String email) {
        Page<User> userPage;
        
        if (username != null && !username.isEmpty() && email != null && !email.isEmpty()) {
            // 同时按用户名和邮箱搜索
            userPage = userRepository.findByUsernameContainingAndEmailContaining(
                username, email, PageRequest.of(page, size));
        } else if (username != null && !username.isEmpty()) {
            // 只按用户名搜索
            userPage = userRepository.findByUsernameContaining(
                username, PageRequest.of(page, size));
        } else if (email != null && !email.isEmpty()) {
            // 只按邮箱搜索
            userPage = userRepository.findByEmailContaining(
                email, PageRequest.of(page, size));
        } else {
            // 没有搜索条件，返回所有用户
            userPage = userRepository.findAll(PageRequest.of(page, size));
        }
        
        return new PageDto<>(
            userPage.getTotalElements(),
            page,
            size,
            userPage.getContent()
        );
    }
}

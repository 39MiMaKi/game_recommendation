package com.wbbb.steam.service;


import com.wbbb.steam.dto.request.UserProfileUpdateDto;
import com.wbbb.steam.dto.response.PageDto;
import com.wbbb.steam.dto.response.data.LoginResponseDto;
import com.wbbb.steam.dto.response.data.UserInfoResponseDto;
import com.wbbb.steam.dto.response.data.UserRatingDto;
import com.wbbb.steam.dto.response.data.UserSimpleDto;
import com.wbbb.steam.entity.App;
import com.wbbb.steam.entity.User;
import com.wbbb.steam.entity.UserRating;
import com.wbbb.steam.repository.UserRepository;
import com.wbbb.steam.repository.UserRatingRepository;
import com.wbbb.steam.repository.AppRepository;

import com.wbbb.steam.util.TokenUtil;
import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
@Service
public class UserService {
    // 添加logger声明
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final UserRatingRepository userRatingRepository;
    private final AppRepository appRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 登录
     * @param username 账户名称
     * @param password 密码
     * @return 登录成功返回token，失败返回null
     */
    public LoginResponseDto login(String username, String password, boolean rememberMe) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            logger.warn("登录失败 - 用户名不存在: {}", username);
            return null;
        }
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("登录失败 - 密码不匹配 (用户名: {})", username);
            return null;
        }
        
        if (!user.getEnabled()) {
            logger.warn("登录失败 - 账户已禁用 (用户ID: {})", user.getUserId());
            return null;
        }
        
        logger.info("登录成功 - 用户ID: {}, 角色: {}", user.getUserId(), user.getRole());
        return new LoginResponseDto(
            TokenUtil.generateToken(user, rememberMe),
            user.getUserId(),
            user.getRole() // 确保返回role
        );
    }

    /**
     * 注册
     * @param email    电子邮箱地址
     * @param username 账户名称
     * @param password 密码
     */
    public void join(String email, String username, String password, List<String> tags) {
        email = email.trim();
        // 密码必须包含大小写字母、数字，且至少8位
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
            throw new IllegalArgumentException("密码必须包含大小写字母、数字，且至少8位");
        }
        
        User user = User.builder()
                .email(email)
                .username(username)
                .password(passwordEncoder.encode(password)) // 确保只使用BCrypt加密
                .nickname(username)
                .avatar("https://steam-1314488277.cos.ap-guangzhou.myqcloud.com/assets/default_avatar.jpg")
                .createTime(LocalDateTime.now())
                .tags(tags != null ? String.join(",", tags) : "")
                .build();
        try {
            if (userRepository.existsByUsername(user.getUsername()))
                throw new RuntimeException("用户名已被注册");
            if (userRepository.existsByEmail(user.getEmail()))
                throw new RuntimeException("邮箱已被注册");
            userRepository.save(user);
        } catch (Exception e) {
            throw new IllegalArgumentException("用户注册失败：" + e.getMessage());
        }
    }

    /**
     * 检测账户名称是否可用
     * @param username 账户名称
     * @return boolean
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * 检测邮箱是否可用
     * @param email 邮箱地址
     * @return boolean
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * 获取账户信息
     * @param userId 账户id
     */
    public User getUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.orElse(null);
    }

    /**
     * 解析Token，并验证userId是否存在
     * @param token Token
     * @return 成功返回userId，失败返回null
     */
    public Long parseToken(String token) {
        Long userId = TokenUtil.parseToken(token);
        if (userId == null || !userRepository.existsById(userId))
            return null;
        return userId;
    }

    /**
     * 验证userId是否存在
     * @param userId userId
     * @return boolean
     */
    public boolean isUserIdExists(Long userId) {
        return userId != null && userRepository.existsById(userId);
    }

    /**
     * 获取用户个人资料
     */
    /**
     * 获取用户个人资料
     */
    public UserInfoResponseDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }
        
        UserInfoResponseDto userInfo = new UserInfoResponseDto();
        userInfo.setUserId(user.getUserId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setEmail(user.getEmail());
        userInfo.setTags(user.getTags());
        userInfo.setCreateTime(user.getCreateTime());
        
        return userInfo;
    }
    
    /**
     * 更新用户个人资料
     */
    public boolean updateUserProfile(Long userId, UserProfileUpdateDto profileUpdateDto) {
        User user = getUser(userId);
        if (user == null) {
            return false;
        }
        
        if (profileUpdateDto.getNickname() != null) {
            user.setNickname(profileUpdateDto.getNickname());
        }
        
        if (profileUpdateDto.getAvatar() != null) {
            user.setAvatar(profileUpdateDto.getAvatar());
        }
        
        if (profileUpdateDto.getTags() != null) {
            user.setTags(String.join(",", profileUpdateDto.getTags()));
        }
        
        userRepository.save(user);
        return true;
    }

    /**
     * 搜索用户
     */
    public PageDto<UserSimpleDto> searchUser(String keyword, Integer pageIndex, Integer pageSize) {
        // 使用已有的分页查询方法
        Page<User> userPage = userRepository.findByUsernameContainingOrNicknameContaining(
                keyword, keyword, PageRequest.of(pageIndex, pageSize));
        
        List<UserSimpleDto> userSimpleDtos = userPage.getContent().stream()
                .map(user -> new UserSimpleDto(user.getUserId(), user.getUsername(), user.getNickname(), user.getAvatar()))
                .collect(Collectors.toList());
        
        return new PageDto<>(userPage.getTotalElements(), pageIndex, pageSize, userSimpleDtos);
    }

    /**
     * 获取用户评分记录
     */
    public PageDto<UserRatingDto> getUserRatings(Long userId, int page, int size) {
        // 获取用户评分记录
        Page<UserRating> ratingsPage = userRatingRepository.findByUserIdOrderByTimestampDesc(
                userId, PageRequest.of(page, size));
        
        List<UserRatingDto> ratingDtos = new ArrayList<>();
        for (UserRating rating : ratingsPage.getContent()) {
            App app = appRepository.findById(rating.getAppId()).orElse(null);
            if (app != null) {
                UserRatingDto dto = new UserRatingDto();
                dto.setAppId(app.getAppId());
                dto.setAppName(app.getName());
                dto.setAppCover(app.getCover());
                // 在steam风格的点赞/点踩系统中，我们直接使用推荐状态
                dto.setRecommended(rating.getRecommended());
                dto.setRatingTime(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(rating.getTimestamp()), ZoneId.systemDefault()));
                ratingDtos.add(dto);
            }
        }
        
        return new PageDto<>(
                ratingsPage.getTotalElements(),
                page,
                size,
                ratingDtos
        );
    }
}

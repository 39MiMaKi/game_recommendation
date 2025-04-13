package com.wbbb.steam.dto.response.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRatingDto {
    private Long appId;
    private String appName;
    private String appCover;
    private Boolean recommended;
    private LocalDateTime ratingTime;
}
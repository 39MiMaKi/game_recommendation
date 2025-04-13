package com.wbbb.steam.dto.response.data;

import lombok.Data;

@Data
public class UserGameStatsDto {
    private Long userId;
    private String username;
    private Integer ratedGamesCount;
}
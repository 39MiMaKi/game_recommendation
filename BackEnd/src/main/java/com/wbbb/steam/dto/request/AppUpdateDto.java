package com.wbbb.steam.dto.request;

import lombok.Data;
import java.util.Set;

@Data
public class AppUpdateDto {
    private String name;
    private String cover;
    private String header;
    private String images;  // 保持String类型
    private String description;
    private String developer;
    private String publisher;
    private Double price;
    private Double finalPrice;
    private Boolean win;
    private Boolean mac;
    private Boolean linux;
    private String createTime;
    private Set<String> tags;
    private Integer popularity;
    private Double averageRating;
}
package com.wbbb.steam.dto.request;

import lombok.Data;
import java.util.Set;

@Data
public class AppFilterDto {
    private Set<String> tags;     // 标签列表
    private Double minPrice;      // 最低价格
    private Double maxPrice;      // 最高价格
    private String startDate;    // 开始日期（格式为"yyyy-MM-dd"的字符串）
    private String endDate;      // 结束日期（格式为"yyyy-MM-dd"的字符串）
}
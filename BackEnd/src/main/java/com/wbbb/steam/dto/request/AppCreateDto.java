package com.wbbb.steam.dto.request;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.*;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppCreateDto {
    @NotBlank(message = "游戏名称不能为空")
    private String name;
    
    @NotBlank(message = "封面图片不能为空")
    private String cover;
    
    private String header;
    private String images;
    
    @Size(max = 4095, message = "描述过长")
    private String description;
    
    private String developer;
    private String publisher;
    
    @DecimalMin(value = "0.0", message = "价格不能为负数")
    private Double price;
    
    @DecimalMin(value = "0.0", message = "最终价格不能为负数")
    private Double finalPrice;
    
    @NotNull(message = "请指定Windows支持状态")
    private Boolean win;
    
    @NotNull(message = "请指定MacOS支持状态")
    private Boolean mac;
    
    @NotNull(message = "请指定Linux支持状态")
    private Boolean linux;
    
    private String createTime;
    
    @NotEmpty(message = "至少需要一个标签")
    private Set<String> tags;
    
    @Min(value = 0, message = "热度值不能为负数")
    private Integer popularity;
}


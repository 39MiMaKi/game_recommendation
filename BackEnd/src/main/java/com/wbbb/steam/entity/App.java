package com.wbbb.steam.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "app")
public class App {

    /** 游戏id */
    @Id
    @Column(name = "app_id")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "app_id_generator")
    @TableGenerator(name = "app_id_generator", table = "id_generator", pkColumnName = "name", valueColumnName = "value",
            pkColumnValue = "app", initialValue = 20250119)
    private Long appId;

    /** 名称 */
    private String name;

    /** 封面图片 */
    private String cover;

    /** 标题图片 */
    private String header;

    /** 图片 */
    @Column(length = 4095)
    private String images;

    /** 介绍 */
    @Column(length = 4095)
    private String description;

    /** 开发商 */
    private String developer;

    /** 发行商 */
    private String publisher;

    /** 价格 */
    private Double price;

    /** 游戏标签 */
    @ElementCollection
    @CollectionTable(name = "app_tags", joinColumns = @JoinColumn(name = "app_id"))
    @Column(name = "tag")
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    /** 当前价格 */
    @Column(name = "final_price")
    private Double finalPrice;

    /** 是否支持Windows */
    @Column(columnDefinition = "BOOLEAN")
    private Boolean win;

    /** 是否支持MacOS */
    @Column(columnDefinition = "BOOLEAN")
    private Boolean mac;

    /** 是否支持Linux */
    @Column(columnDefinition = "BOOLEAN")
    private Boolean linux;

    /** 热度值 */
    private Integer popularity;

    /** 发行时间 */
    @Column(name = "create_time", length = 10)
    private String createTime;

    /** 好评率 */
    @Column(name = "positive_rate", columnDefinition = "DECIMAL(5,2) DEFAULT 0.00")
    private Double positiveRate;
    
    /** 评价总数 */
    @Column(name = "review_count", columnDefinition = "INT DEFAULT 0")
    private Integer reviewCount;

    @Builder
    public App(String name, String cover, String header, String images, String description, 
               String developer, String publisher, Double price, Double finalPrice, 
               boolean win, boolean mac, boolean linux, String createTime, 
               Set<String> tags, Integer popularity, Double positiveRate) {
        this.name = name;
        this.cover = cover;
        this.header = header;
        this.images = images;
        this.description = description;
        this.developer = developer;
        this.publisher = publisher;
        this.price = price;
        this.finalPrice = finalPrice;
        this.win = win;
        this.mac = mac;
        this.linux = linux;
        this.createTime = createTime; // 现在接收格式为"2012-08-21"的字符串
        // 确保tags只包含schema.sql中定义的合法值
        this.tags = tags != null ? tags.stream()
            .filter(t -> Set.of("动作", "冒险", "角色扮演", "射击", "策略", "体育", "模拟", "解谜", "竞速", "格斗").contains(t))
            .collect(Collectors.toSet()) : new HashSet<>();
        this.popularity = popularity;
        this.positiveRate = positiveRate;
    }

  
}

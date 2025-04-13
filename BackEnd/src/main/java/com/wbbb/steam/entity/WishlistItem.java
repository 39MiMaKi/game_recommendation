package com.wbbb.steam.entity;

import jakarta.persistence.*;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

@Builder
@Entity
@Table(name = "wishlist_item")
@IdClass(WishlistItemId.class)
public class WishlistItem {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "app_id")
    private Long appId;

    private Integer sort = 0;

    @Column(name = "create_time")
    private Long createTime;

    @Builder
    public WishlistItem(Long userId, Long appId, Integer sort, Long createTime) {
        this.userId = userId;
        this.appId = appId;
        this.sort = sort != null ? sort : 0;
        this.createTime = createTime != null ? createTime : System.currentTimeMillis();
    }
}

package com.wbbb.steam.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class UserProfileUpdateDto {
    private String nickname;
    private String avatar;
    private List<String> tags;
}
package com.couple.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WishRequest {
    @NotBlank(message = "心愿名称不能为空")
    private String title;

    private String description;
}

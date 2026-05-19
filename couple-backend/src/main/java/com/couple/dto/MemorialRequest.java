package com.couple.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MemorialRequest {
    @NotBlank(message = "纪念日名称不能为空")
    private String title;

    @NotNull(message = "日期不能为空")
    private LocalDate eventDate;

    private String description;
}

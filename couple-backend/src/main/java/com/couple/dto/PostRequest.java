package com.couple.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class PostRequest {
    @NotBlank(message = "类型不能为空")
    private String postType;  // image / video

    private String content;   // 配文

    private List<String> files;  // 已上传的文件 URL 列表
}

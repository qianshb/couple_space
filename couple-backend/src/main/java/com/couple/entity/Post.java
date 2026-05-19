package com.couple.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("posts")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long coupleId;
    private String content;
    private String postType;  // image / video
    private String files;     // JSON string of URLs
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

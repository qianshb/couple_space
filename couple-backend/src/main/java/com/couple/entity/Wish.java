package com.couple.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wishes")
public class Wish {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long coupleId;
    private Long userId;
    private String title;
    private String description;
    private Boolean fulfilled;
    private LocalDateTime createdAt;
}

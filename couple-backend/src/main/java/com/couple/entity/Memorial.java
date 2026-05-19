package com.couple.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("memorials")
public class Memorial {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long coupleId;
    private Long userId;
    private String title;
    private LocalDate eventDate;
    private String description;
    private LocalDateTime createdAt;
}

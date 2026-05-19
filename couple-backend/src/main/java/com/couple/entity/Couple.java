package com.couple.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("couples")
public class Couple {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long user1Id;
    private Long user2Id;
    private String inviteCode;
    private Integer status;  // 0=pending, 1=bound
    private LocalDateTime boundAt;
    private LocalDateTime createdAt;
}

package com.couple.dto;

import lombok.Data;

@Data
public class CoupleStatusResponse {
    private boolean bound;            // 是否已绑定
    private String myInviteCode;      // 我的邀请码
    private String partnerNickname;   // 对方昵称（已绑定时有值）
    private String partnerAvatar;
    private String partnerCity;       // 对方所在城市
}

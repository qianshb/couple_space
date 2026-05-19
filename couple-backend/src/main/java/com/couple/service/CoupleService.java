package com.couple.service;

import com.couple.dto.CoupleStatusResponse;

public interface CoupleService {
    String generateInviteCode(Long userId);
    void bind(Long userId, String inviteCode);
    CoupleStatusResponse getStatus(Long userId);
}

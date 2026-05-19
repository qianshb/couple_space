package com.couple.service;

import com.couple.dto.PartnerResponse;
import com.couple.dto.UpdateUserRequest;
import com.couple.dto.UserResponse;

public interface UserService {
    UserResponse getMe(Long userId);
    UserResponse updateMe(Long userId, UpdateUserRequest req);
    PartnerResponse getPartner(Long userId);
}

package com.couple.service;

import com.couple.dto.WishRequest;
import com.couple.dto.WishResponse;

import java.util.List;

public interface WishService {
    WishResponse create(Long userId, WishRequest req);
    List<WishResponse> list(Long userId);
    WishResponse toggle(Long userId, Long id);
    void delete(Long userId, Long id);
}

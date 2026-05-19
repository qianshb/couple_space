package com.couple.service;

import com.couple.dto.MemorialRequest;
import com.couple.dto.MemorialResponse;

import java.util.List;

public interface MemorialService {
    MemorialResponse create(Long userId, MemorialRequest req);
    List<MemorialResponse> list(Long userId);
    void delete(Long userId, Long id);
}

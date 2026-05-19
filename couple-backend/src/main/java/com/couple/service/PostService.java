package com.couple.service;

import com.couple.dto.PostRequest;
import com.couple.dto.PostResponse;
import java.util.List;

public interface PostService {
    PostResponse create(Long userId, PostRequest req);
    List<PostResponse> list(Long userId, int page, int size);
    void delete(Long userId, Long postId);
}

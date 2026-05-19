package com.couple.dto;

import com.couple.entity.Post;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
public class PostResponse {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Long id;
    private Long userId;
    private String nickname;
    private String avatar;
    private String content;
    private String postType;
    private List<String> files;
    private LocalDateTime createdAt;

    public static PostResponse from(Post post, String nickname, String avatar) {
        PostResponse r = new PostResponse();
        r.id = post.getId();
        r.userId = post.getUserId();
        r.nickname = nickname;
        r.avatar = avatar;
        r.content = post.getContent();
        r.postType = post.getPostType();
        try {
            r.files = MAPPER.readValue(post.getFiles(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            r.files = Collections.emptyList();
        }
        r.createdAt = post.getCreatedAt();
        return r;
    }
}

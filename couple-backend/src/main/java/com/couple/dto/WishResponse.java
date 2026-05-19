package com.couple.dto;

import com.couple.entity.Wish;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WishResponse {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private Boolean fulfilled;
    private LocalDateTime createdAt;

    public static WishResponse from(Wish w) {
        WishResponse r = new WishResponse();
        r.id = w.getId();
        r.userId = w.getUserId();
        r.title = w.getTitle();
        r.description = w.getDescription();
        r.fulfilled = w.getFulfilled() != null && w.getFulfilled();
        r.createdAt = w.getCreatedAt();
        return r;
    }
}

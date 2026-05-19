package com.couple.dto;

import com.couple.entity.Memorial;
import lombok.Data;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Data
public class MemorialResponse {
    private Long id;
    private String title;
    private LocalDate eventDate;
    private String description;
    private long daysPassed;

    public static MemorialResponse from(Memorial m) {
        MemorialResponse r = new MemorialResponse();
        r.id = m.getId();
        r.title = m.getTitle();
        r.eventDate = m.getEventDate();
        r.description = m.getDescription();
        r.daysPassed = ChronoUnit.DAYS.between(m.getEventDate(), LocalDate.now());
        return r;
    }
}

package com.couple.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    private String nickname;
    private String avatar;
    private LocalDate birthday;
    private String city;
}

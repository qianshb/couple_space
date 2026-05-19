package com.couple.dto;

import com.couple.entity.User;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private LocalDate birthday;
    private String constellation;
    private String city;
    private Double latitude;
    private Double longitude;
    private String ip;

    public static UserResponse from(User user) {
        UserResponse r = new UserResponse();
        r.id = user.getId();
        r.username = user.getUsername();
        r.nickname = user.getNickname();
        r.avatar = user.getAvatar();
        r.birthday = user.getBirthday();
        r.constellation = getConstellation(user.getBirthday());
        r.city = user.getCity();
        r.latitude = user.getLatitude();
        r.longitude = user.getLongitude();
        r.ip = user.getIp();
        return r;
    }

    private static String getConstellation(LocalDate birthday) {
        if (birthday == null) return null;
        int month = birthday.getMonthValue();
        int day = birthday.getDayOfMonth();
        if ((month == 1 && day >= 20) || (month == 2 && day <= 18)) return "水瓶座";
        if ((month == 2 && day >= 19) || (month == 3 && day <= 20)) return "双鱼座";
        if ((month == 3 && day >= 21) || (month == 4 && day <= 19)) return "白羊座";
        if ((month == 4 && day >= 20) || (month == 5 && day <= 20)) return "金牛座";
        if ((month == 5 && day >= 21) || (month == 6 && day <= 21)) return "双子座";
        if ((month == 6 && day >= 22) || (month == 7 && day <= 22)) return "巨蟹座";
        if ((month == 7 && day >= 23) || (month == 8 && day <= 22)) return "狮子座";
        if ((month == 8 && day >= 23) || (month == 9 && day <= 22)) return "处女座";
        if ((month == 9 && day >= 23) || (month == 10 && day <= 23)) return "天秤座";
        if ((month == 10 && day >= 24) || (month == 11 && day <= 22)) return "天蝎座";
        if ((month == 11 && day >= 23) || (month == 12 && day <= 21)) return "射手座";
        return "摩羯座";
    }
}

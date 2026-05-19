package com.couple.util;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.InetAddress;

@Component
public class GeoIpUtil {

    @Value("${app.geoip.db-path}")
    private String dbPath;

    private DatabaseReader reader;

    @PostConstruct
    public void init() {
        try {
            InputStream input = getClass().getClassLoader()
                    .getResourceAsStream(dbPath.replace("classpath:", ""));
            if (input != null) {
                reader = new DatabaseReader.Builder(input).build();
            }
        } catch (Exception ignored) {
            // GeoIP 数据库未放置时忽略，city 将返回 "未知"
        }
    }

    public String getCity(String ip) {
        if (reader == null || ip == null || ip.isBlank()
                || ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {
            return "未知";
        }
        try {
            InetAddress addr = InetAddress.getByName(ip);
            CityResponse response = reader.city(addr);
            String city = response.getCity().getName();
            return city != null ? city : "未知";
        } catch (Exception e) {
            return "未知";
        }
    }
}

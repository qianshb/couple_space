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

    @Value("${app.geoip.mock-ip:}")
    private String mockIp;

    private DatabaseReader reader;

    public record GeoIpInfo(String city, String country, Double latitude, Double longitude) {}

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

    public GeoIpInfo getInfo(String ip) {
        // 本地测试：mock-ip 配置了且客户端是本地地址时，替换 IP
        if (!mockIp.isBlank()
                && (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1"))) {
            ip = mockIp;
        }
        if (reader == null || ip == null || ip.isBlank()
                || ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {
            return new GeoIpInfo("未知", null, null, null);
        }
        try {
            InetAddress addr = InetAddress.getByName(ip);
            CityResponse response = reader.city(addr);
            String city = response.getCity().getName();
            String country = response.getCountry().getName();
            Double lat = response.getLocation().getLatitude();
            Double lng = response.getLocation().getLongitude();
            return new GeoIpInfo(city != null ? city : "未知", country, lat, lng);
        } catch (Exception e) {
            return new GeoIpInfo("未知", null, null, null);
        }
    }

    public String getCity(String ip) {
        return getInfo(ip).city();
    }
}

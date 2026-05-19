package com.couple.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.couple.dto.LoginResponse;
import com.couple.dto.RegisterRequest;
import com.couple.dto.UserResponse;
import com.couple.entity.User;
import com.couple.exception.BusinessException;
import com.couple.mapper.UserMapper;
import com.couple.service.AuthService;
import com.couple.util.GeoIpUtil;
import com.couple.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final GeoIpUtil geoIpUtil;
    private final HttpServletRequest request;

    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil, GeoIpUtil geoIpUtil, HttpServletRequest request) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.geoIpUtil = geoIpUtil;
        this.request = request;
    }

    @Override
    public LoginResponse register(RegisterRequest req) {
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getUsername()); // 默认昵称

        // IP 定位城市
        String ip = getClientIp();
        var info = geoIpUtil.getInfo(ip);
        user.setCity(info.city());
        user.setLatitude(info.latitude());
        user.setLongitude(info.longitude());
        user.setIp(ip);

        userMapper.insert(user);

        String token = jwtUtil.generateToken(user.getId());
        return new LoginResponse(token, UserResponse.from(user));
    }

    @Override
    public LoginResponse login(String username, String password) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 更新城市（GeoIP 返回真实城市时才更新）
        String ip = getClientIp();
        var info = geoIpUtil.getInfo(ip);
        if (!"未知".equals(info.city()) && !info.city().equals(user.getCity())) {
            user.setCity(info.city());
            user.setLatitude(info.latitude());
            user.setLongitude(info.longitude());
            user.setIp(ip);
            userMapper.updateById(user);
        }

        String token = jwtUtil.generateToken(user.getId());
        return new LoginResponse(token, UserResponse.from(user));
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

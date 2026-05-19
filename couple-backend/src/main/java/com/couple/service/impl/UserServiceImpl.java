package com.couple.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.couple.dto.PartnerResponse;
import com.couple.dto.UpdateUserRequest;
import com.couple.dto.UserResponse;
import com.couple.entity.Couple;
import com.couple.entity.User;
import com.couple.exception.BusinessException;
import com.couple.mapper.CoupleMapper;
import com.couple.mapper.UserMapper;
import com.couple.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final CoupleMapper coupleMapper;

    public UserServiceImpl(UserMapper userMapper, CoupleMapper coupleMapper) {
        this.userMapper = userMapper;
        this.coupleMapper = coupleMapper;
    }

    @Override
    public UserResponse getMe(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return UserResponse.from(user);
    }

    @Override
    public UserResponse updateMe(Long userId, UpdateUserRequest req) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (StringUtils.hasText(req.getNickname())) {
            user.setNickname(req.getNickname());
        }
        if (req.getAvatar() != null) {
            user.setAvatar(req.getAvatar());
        }
        if (req.getBirthday() != null) {
            user.setBirthday(req.getBirthday());
        }
        if (StringUtils.hasText(req.getCity())) {
            user.setCity(req.getCity());
        }
        userMapper.updateById(user);
        return UserResponse.from(user);
    }

    @Override
    public PartnerResponse getPartner(Long userId) {
        // 找到两人的绑定关系
        Couple couple = coupleMapper.selectOne(
                new LambdaQueryWrapper<Couple>()
                        .eq(Couple::getStatus, 1)
                        .and(w -> w.eq(Couple::getUser1Id, userId)
                                .or().eq(Couple::getUser2Id, userId)));
        if (couple == null) {
            throw new BusinessException("尚未绑定情侣关系");
        }

        Long partnerId = couple.getUser1Id().equals(userId)
                ? couple.getUser2Id() : couple.getUser1Id();

        User partner = userMapper.selectById(partnerId);
        if (partner == null) {
            throw new BusinessException("对方用户不存在");
        }

        PartnerResponse resp = new PartnerResponse();
        resp.setId(partner.getId());
        resp.setNickname(partner.getNickname());
        resp.setAvatar(partner.getAvatar());
        resp.setCity(partner.getCity());
        resp.setLatitude(partner.getLatitude());
        resp.setLongitude(partner.getLongitude());
        resp.setIp(partner.getIp());
        return resp;
    }
}

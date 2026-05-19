package com.couple.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.couple.dto.CoupleStatusResponse;
import com.couple.entity.Couple;
import com.couple.entity.User;
import com.couple.exception.BusinessException;
import com.couple.mapper.CoupleMapper;
import com.couple.mapper.UserMapper;
import com.couple.service.CoupleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class CoupleServiceImpl implements CoupleService {

    private final CoupleMapper coupleMapper;
    private final UserMapper userMapper;
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public CoupleServiceImpl(CoupleMapper coupleMapper, UserMapper userMapper) {
        this.coupleMapper = coupleMapper;
        this.userMapper = userMapper;
    }

    @Override
    public String generateInviteCode(Long userId) {
        // 检查是否已有待绑定的邀请码
        Couple existing = coupleMapper.selectOne(
                new LambdaQueryWrapper<Couple>()
                        .eq(Couple::getUser1Id, userId)
                        .eq(Couple::getStatus, 0));
        if (existing != null) {
            return existing.getInviteCode();
        }

        // 检查是否已有绑定关系
        Long bound = coupleMapper.selectCount(
                new LambdaQueryWrapper<Couple>()
                        .eq(Couple::getStatus, 1)
                        .and(w -> w.eq(Couple::getUser1Id, userId)
                                .or().eq(Couple::getUser2Id, userId)));
        if (bound > 0) {
            throw new BusinessException("你已绑定情侣关系，不能重复创建邀请");
        }

        String code = generateCode();
        Couple couple = new Couple();
        couple.setUser1Id(userId);
        couple.setInviteCode(code);
        couple.setStatus(0);
        coupleMapper.insert(couple);
        return code;
    }

    @Override
    @Transactional
    public void bind(Long userId, String inviteCode) {
        // 检查是否已绑定
        Long bound = coupleMapper.selectCount(
                new LambdaQueryWrapper<Couple>()
                        .eq(Couple::getStatus, 1)
                        .and(w -> w.eq(Couple::getUser1Id, userId)
                                .or().eq(Couple::getUser2Id, userId)));
        if (bound > 0) {
            throw new BusinessException("你已绑定情侣关系");
        }

        // 查找邀请码对应的等待记录
        Couple couple = coupleMapper.selectOne(
                new LambdaQueryWrapper<Couple>()
                        .eq(Couple::getInviteCode, inviteCode)
                        .eq(Couple::getStatus, 0));
        if (couple == null) {
            throw new BusinessException("邀请码无效或已被使用");
        }

        // 不能绑定自己
        if (couple.getUser1Id().equals(userId)) {
            throw new BusinessException("不能绑定自己");
        }

        couple.setUser2Id(userId);
        couple.setStatus(1);
        couple.setBoundAt(LocalDateTime.now());
        coupleMapper.updateById(couple);
    }

    @Override
    public CoupleStatusResponse getStatus(Long userId) {
        CoupleStatusResponse resp = new CoupleStatusResponse();

        // 找等待中的
        Couple pending = coupleMapper.selectOne(
                new LambdaQueryWrapper<Couple>()
                        .eq(Couple::getUser1Id, userId)
                        .eq(Couple::getStatus, 0));
        if (pending != null) {
            resp.setBound(false);
            resp.setMyInviteCode(pending.getInviteCode());
            return resp;
        }

        // 找已绑定的
        Couple boundCouple = coupleMapper.selectOne(
                new LambdaQueryWrapper<Couple>()
                        .eq(Couple::getStatus, 1)
                        .and(w -> w.eq(Couple::getUser1Id, userId)
                                .or().eq(Couple::getUser2Id, userId)));
        if (boundCouple != null) {
            resp.setBound(true);
            Long partnerId = boundCouple.getUser1Id().equals(userId)
                    ? boundCouple.getUser2Id() : boundCouple.getUser1Id();
            User partner = userMapper.selectById(partnerId);
            if (partner != null) {
                resp.setPartnerNickname(partner.getNickname());
                resp.setPartnerAvatar(partner.getAvatar());
                resp.setPartnerCity(partner.getCity());
            }
            return resp;
        }

        resp.setBound(false);
        return resp;
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        // 确保唯一
        Long exists = coupleMapper.selectCount(
                new LambdaQueryWrapper<Couple>().eq(Couple::getInviteCode, sb.toString()));
        if (exists > 0) {
            return generateCode();
        }
        return sb.toString();
    }
}

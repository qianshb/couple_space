package com.couple.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.couple.dto.WishRequest;
import com.couple.dto.WishResponse;
import com.couple.entity.Couple;
import com.couple.entity.Wish;
import com.couple.exception.BusinessException;
import com.couple.mapper.CoupleMapper;
import com.couple.mapper.WishMapper;
import com.couple.service.WishService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishServiceImpl implements WishService {

    private final WishMapper wishMapper;
    private final CoupleMapper coupleMapper;

    public WishServiceImpl(WishMapper wishMapper, CoupleMapper coupleMapper) {
        this.wishMapper = wishMapper;
        this.coupleMapper = coupleMapper;
    }

    @Override
    public WishResponse create(Long userId, WishRequest req) {
        Long coupleId = getCoupleId(userId);

        Wish w = new Wish();
        w.setCoupleId(coupleId);
        w.setUserId(userId);
        w.setTitle(req.getTitle());
        w.setDescription(req.getDescription());
        w.setFulfilled(false);
        wishMapper.insert(w);

        return WishResponse.from(w);
    }

    @Override
    public List<WishResponse> list(Long userId) {
        Long coupleId = getCoupleId(userId);

        LambdaQueryWrapper<Wish> wrapper = new LambdaQueryWrapper<Wish>()
                .eq(Wish::getCoupleId, coupleId)
                .orderByAsc(Wish::getFulfilled)
                .orderByDesc(Wish::getCreatedAt);

        return wishMapper.selectList(wrapper)
                .stream()
                .map(WishResponse::from)
                .toList();
    }

    @Override
    public WishResponse toggle(Long userId, Long id) {
        Long coupleId = getCoupleId(userId);

        Wish w = wishMapper.selectById(id);
        if (w == null) {
            throw new BusinessException("心愿不存在");
        }
        if (!w.getCoupleId().equals(coupleId)) {
            throw new BusinessException("无权操作");
        }

        w.setFulfilled(!(w.getFulfilled() != null && w.getFulfilled()));
        wishMapper.updateById(w);

        return WishResponse.from(w);
    }

    @Override
    public void delete(Long userId, Long id) {
        Long coupleId = getCoupleId(userId);

        Wish w = wishMapper.selectById(id);
        if (w == null) {
            throw new BusinessException("心愿不存在");
        }
        if (!w.getCoupleId().equals(coupleId)) {
            throw new BusinessException("无权删除");
        }
        wishMapper.deleteById(id);
    }

    private Long getCoupleId(Long userId) {
        Couple couple = coupleMapper.selectOne(new LambdaQueryWrapper<Couple>()
                .eq(Couple::getStatus, 1)
                .and(w -> w.eq(Couple::getUser1Id, userId)
                        .or().eq(Couple::getUser2Id, userId)));
        if (couple == null) {
            throw new BusinessException("请先绑定情侣");
        }
        return couple.getId();
    }
}

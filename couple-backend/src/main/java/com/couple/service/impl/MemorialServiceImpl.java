package com.couple.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.couple.dto.MemorialRequest;
import com.couple.dto.MemorialResponse;
import com.couple.entity.Couple;
import com.couple.entity.Memorial;
import com.couple.exception.BusinessException;
import com.couple.mapper.CoupleMapper;
import com.couple.mapper.MemorialMapper;
import com.couple.service.MemorialService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemorialServiceImpl implements MemorialService {

    private final MemorialMapper memorialMapper;
    private final CoupleMapper coupleMapper;

    public MemorialServiceImpl(MemorialMapper memorialMapper, CoupleMapper coupleMapper) {
        this.memorialMapper = memorialMapper;
        this.coupleMapper = coupleMapper;
    }

    @Override
    public MemorialResponse create(Long userId, MemorialRequest req) {
        Long coupleId = getCoupleId(userId);

        Memorial m = new Memorial();
        m.setCoupleId(coupleId);
        m.setUserId(userId);
        m.setTitle(req.getTitle());
        m.setEventDate(req.getEventDate());
        m.setDescription(req.getDescription());
        memorialMapper.insert(m);

        return MemorialResponse.from(m);
    }

    @Override
    public List<MemorialResponse> list(Long userId) {
        Long coupleId = getCoupleId(userId);

        LambdaQueryWrapper<Memorial> wrapper = new LambdaQueryWrapper<Memorial>()
                .eq(Memorial::getCoupleId, coupleId)
                .orderByDesc(Memorial::getEventDate);

        return memorialMapper.selectList(wrapper)
                .stream()
                .map(MemorialResponse::from)
                .toList();
    }

    @Override
    public void delete(Long userId, Long id) {
        Long coupleId = getCoupleId(userId);

        Memorial m = memorialMapper.selectById(id);
        if (m == null) {
            throw new BusinessException("纪念日不存在");
        }
        if (!m.getCoupleId().equals(coupleId)) {
            throw new BusinessException("无权删除");
        }
        memorialMapper.deleteById(id);
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

package com.couple.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.couple.dto.PostRequest;
import com.couple.dto.PostResponse;
import com.couple.entity.Couple;
import com.couple.entity.Post;
import com.couple.entity.User;
import com.couple.exception.BusinessException;
import com.couple.mapper.CoupleMapper;
import com.couple.mapper.PostMapper;
import com.couple.mapper.UserMapper;
import com.couple.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final CoupleMapper coupleMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public PostServiceImpl(PostMapper postMapper, CoupleMapper coupleMapper,
                           UserMapper userMapper, ObjectMapper objectMapper) {
        this.postMapper = postMapper;
        this.coupleMapper = coupleMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public PostResponse create(Long userId, PostRequest req) {
        Couple couple = coupleMapper.selectOne(
                new LambdaQueryWrapper<Couple>()
                        .eq(Couple::getStatus, 1)
                        .and(w -> w.eq(Couple::getUser1Id, userId)
                                .or().eq(Couple::getUser2Id, userId)));
        if (couple == null) {
            throw new BusinessException("请先绑定情侣关系");
        }

        Post post = new Post();
        post.setUserId(userId);
        post.setCoupleId(couple.getId());
        post.setContent(req.getContent());
        post.setPostType(req.getPostType());
        try {
            post.setFiles(objectMapper.writeValueAsString(req.getFiles()));
        } catch (Exception e) {
            throw new BusinessException("文件数据异常");
        }
        postMapper.insert(post);

        User user = userMapper.selectById(userId);
        return PostResponse.from(post, user.getNickname(), user.getAvatar());
    }

    @Override
    public List<PostResponse> list(Long userId, int page, int size) {
        Couple couple = coupleMapper.selectOne(
                new LambdaQueryWrapper<Couple>()
                        .eq(Couple::getStatus, 1)
                        .and(w -> w.eq(Couple::getUser1Id, userId)
                                .or().eq(Couple::getUser2Id, userId)));
        if (couple == null) {
            throw new BusinessException("请先绑定情侣关系");
        }

        Page<Post> pageReq = new Page<>(page, size);
        Page<Post> result = postMapper.selectPage(pageReq,
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getCoupleId, couple.getId())
                        .orderByDesc(Post::getCreatedAt));

        return result.getRecords().stream().map(p -> {
            User user = userMapper.selectById(p.getUserId());
            String nickname = user != null ? user.getNickname() : "未知";
            String avatar = user != null ? user.getAvatar() : null;
            return PostResponse.from(p, nickname, avatar);
        }).collect(Collectors.toList());
    }

    @Override
    public void delete(Long userId, Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("动态不存在");
        }
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的动态");
        }
        postMapper.deleteById(postId);
    }
}

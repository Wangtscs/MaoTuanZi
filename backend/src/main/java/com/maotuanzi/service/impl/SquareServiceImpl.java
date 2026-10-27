package com.maotuanzi.service.impl;

import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.dto.PostCreateRequest;
import com.maotuanzi.entity.PostComment;
import com.maotuanzi.entity.PostLike;
import com.maotuanzi.entity.PostReport;
import com.maotuanzi.entity.SquarePost;
import com.maotuanzi.mapper.SquareMapper;
import com.maotuanzi.security.UserContext;
import com.maotuanzi.service.AuditService;
import com.maotuanzi.service.SquareService;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SquareServiceImpl implements SquareService {

    private final SquareMapper squareMapper;
    private final AuditService auditService;

    @Override
    public PageResult<SquarePost> list(String tab, String topic, Integer status, int page, int pageSize) {
        // 默认仅展示已发布
        Integer st = status != null ? status : 1;
        PageHelper.startPage(page, pageSize);
        List<SquarePost> list;
        if (tab != null && !tab.isBlank()) {
            list = squareMapper.selectPostsByTab(tab, st, (page - 1) * pageSize, pageSize);
        } else {
            list = squareMapper.selectPosts(topic, st, (page - 1) * pageSize, pageSize);
        }
        return PageResult.of(list);
    }

    @Override
    public SquarePost detail(Long id) {
        SquarePost p = squareMapper.selectPostById(id);
        if (p == null) throw BizException.of(ErrorCode.POST_NOT_FOUND);
        return p;
    }

    @Override
    @Transactional
    public SquarePost publish(PostCreateRequest req) {
        Long uid = UserContext.getUserId();
        if (uid == null) throw BizException.of(ErrorCode.UNAUTHORIZED);

        // 内容审核
        boolean pass = auditService.checkText(req.getContent());
        if (!pass) throw BizException.of(ErrorCode.POST_CONTENT_INVALID);

        SquarePost p = new SquarePost();
        p.setUserId(uid);
        p.setWorkId(req.getWorkId());
        p.setPetId(req.getPetId());
        p.setContent(req.getContent());
        p.setImageUrls(req.getImageUrls() != null ? String.join(",", req.getImageUrls()) : null);
        p.setTopic(req.getTopic());
        p.setIs3d(req.getIs3d() != null ? req.getIs3d() : 0);
        p.setStatus(1);
        squareMapper.insertPost(p);
        return p;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long uid = UserContext.getUserId();
        SquarePost p = squareMapper.selectPostById(id);
        if (p == null || !p.getUserId().equals(uid)) {
            throw BizException.of(ErrorCode.POST_NOT_FOUND);
        }
        squareMapper.softDeletePost(id, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void like(Long postId) {
        Long uid = UserContext.getUserId();
        if (uid == null) throw BizException.of(ErrorCode.UNAUTHORIZED);
        if (squareMapper.selectPostById(postId) == null) throw BizException.of(ErrorCode.POST_NOT_FOUND);
        Integer exist = squareMapper.countLike(postId, uid);
        if (exist != null && exist > 0) throw BizException.of(ErrorCode.ALREADY_LIKED);
        PostLike like = new PostLike();
        like.setPostId(postId);
        like.setUserId(uid);
        squareMapper.insertLike(like);
        squareMapper.incrLikeCount(postId, 1);
    }

    @Override
    @Transactional
    public void unlike(Long postId) {
        Long uid = UserContext.getUserId();
        squareMapper.deleteLike(postId, uid);
        squareMapper.incrLikeCount(postId, -1);
    }

    @Override
    public void report(Long postId, Integer type, String reason) {
        Long uid = UserContext.getUserId();
        PostReport r = new PostReport();
        r.setPostId(postId);
        r.setUserId(uid);
        r.setType(type);
        r.setReason(reason);
        r.setStatus(0);
        squareMapper.insertReport(r);
    }

    @Override
    public List<PostComment> comments(Long postId) {
        return squareMapper.selectComments(postId);
    }

    @Override
    public void addComment(Long postId, String content) {
        Long uid = UserContext.getUserId();
        PostComment c = new PostComment();
        c.setPostId(postId);
        c.setUserId(uid);
        c.setContent(content);
        c.setStatus(1);
        squareMapper.insertComment(c);
    }
}
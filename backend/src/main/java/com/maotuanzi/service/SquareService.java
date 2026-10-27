package com.maotuanzi.service;

import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.dto.PostCreateRequest;
import com.maotuanzi.entity.PostComment;
import com.maotuanzi.entity.SquarePost;

import java.util.List;

public interface SquareService {
    PageResult<SquarePost> list(String tab, String topic, Integer status, int page, int pageSize);
    SquarePost detail(Long id);
    SquarePost publish(PostCreateRequest req);
    void delete(Long id);
    void like(Long postId);
    void unlike(Long postId);
    void report(Long postId, Integer type, String reason);
    List<PostComment> comments(Long postId);
    void addComment(Long postId, String content);
}
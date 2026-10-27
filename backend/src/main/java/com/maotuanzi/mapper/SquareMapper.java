package com.maotuanzi.mapper;

import com.maotuanzi.entity.SquarePost;
import com.maotuanzi.entity.PostLike;
import com.maotuanzi.entity.PostComment;
import com.maotuanzi.entity.PostReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SquareMapper {
    int insertPost(SquarePost post);
    int updatePost(SquarePost post);
    int softDeletePost(@Param("id") Long id, @Param("now") java.time.LocalDateTime now);
    SquarePost selectPostById(@Param("id") Long id);
    List<SquarePost> selectPosts(@Param("topic") String topic, @Param("status") Integer status,
                                 @Param("offset") int offset, @Param("limit") int limit);
    Long countPosts(@Param("topic") String topic, @Param("status") Integer status);

    List<SquarePost> selectPostsByTab(@Param("tab") String tab, @Param("status") Integer status,
                                      @Param("offset") int offset, @Param("limit") int limit);
    Long countPostsByTab(@Param("tab") String tab, @Param("status") Integer status);
    int incrLikeCount(@Param("id") Long id, @Param("delta") int delta);

    int incrShareCount(@Param("id") Long id, @Param("delta") int delta);

    int insertLike(PostLike like);
    int deleteLike(@Param("postId") Long postId, @Param("userId") Long userId);
    int countLike(@Param("postId") Long postId, @Param("userId") Long userId);

    int insertComment(PostComment c);
    int deleteComment(@Param("id") Long id);
    List<PostComment> selectComments(@Param("postId") Long postId);

    int insertReport(PostReport r);
    int updateReport(PostReport r);
    List<PostReport> selectReports(@Param("status") Integer status, @Param("offset") int offset, @Param("limit") int limit);
    Long countReports(@Param("status") Integer status);
}
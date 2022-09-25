package com.serein.community.service;

import com.serein.community.entity.Comment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentService {
    List<Comment> selectCommentsByEntity(@Param("entityType") int entityType, @Param("entityId") Long entityId);

    int insertComment(Comment comment);

    List<Comment> findReplyByUser(Long userId);

    Comment selectCommentById(Long id);
}

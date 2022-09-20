package com.serein.community.mapper;

import com.serein.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface CommentMapper {
    List<Comment> selectCommentsByEntity(@Param("entityType") int entityType,@Param("entityId") Long entityId);

    int selectCountByEntity(@Param("entityType") int entityType,@Param("entityId") Long entityId);

    int insertComment(Comment comment);

    Comment selectByCommentById(Long id);

    List<Comment> selectCommentsByUser(Long userId);

    int selectCountByUser(Long userId);
}

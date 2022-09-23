package com.serein.community.service.impl;

import com.serein.community.entity.Comment;
import com.serein.community.mapper.CommentMapper;
import com.serein.community.mapper.DiscussPostMapper;
import com.serein.community.service.CommentService;
import com.serein.community.util.CommunityConstant;
import com.serein.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Override
    public List<Comment> selectCommentsByEntity(int entityType, Long entityId) {
        return commentMapper.selectCommentsByEntity(entityType,entityId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    @Override
    public int insertComment(Comment comment) {
        if(comment==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        // 敏感词过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));

        // 更新帖子评论数量而非评论的评论
        if(comment.getEntityType() == CommunityConstant.ENTITY_TYPE_POST){
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostMapper.updateCommentCount(comment.getEntityId(),count+1);
        }
        return commentMapper.insertComment(comment);
    }

    @Override
    public List<Comment> findReplyByUser(Long userId) {
        return commentMapper.selectCommentsByTarget(userId);
    }
}

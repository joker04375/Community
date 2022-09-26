package com.serein.community.controller;

import com.serein.community.annotation.LoginRequired;
import com.serein.community.entity.Comment;
import com.serein.community.entity.DiscussPost;
import com.serein.community.entity.Event;
import com.serein.community.entity.User;
import com.serein.community.event.EventProducer;
import com.serein.community.service.CommentService;
import com.serein.community.service.DiscussPostService;
import com.serein.community.util.CommunityConstant;
import com.serein.community.util.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.HashMap;

@Controller
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @LoginRequired
    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId")Long discussPostId, Comment comment){
        User user = UserHolder.getUser();
        comment.setUser(user);
        comment.setUserId(user.getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        System.out.println("comment: "+ comment);
        commentService.insertComment(comment);

        // 触发评论事件
        Event event = new Event();
        event.setTopic(CommunityConstant.TOPIC_COMMENT);
        event.setUserId(user.getId());
        event.setEntityType(comment.getEntityType());
        event.setEntityId(comment.getEntityId());

        event.setData("postId",discussPostId);
        if(comment.getEntityType() == CommunityConstant.ENTITY_TYPE_POST){
            // 查询评论目标 用户
            DiscussPost target = discussPostService.selectDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        else if(comment.getEntityType() == CommunityConstant.ENTITY_TYPE_COMMENT){
            // 回复目标 用户
            Comment target = commentService.selectCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }

        // 调用生产者
        eventProducer.fireEvent(event);


        // 触发发帖事件，修改评论数量
        if(comment.getEntityType() == CommunityConstant.ENTITY_TYPE_POST){
            event = new Event();
            event.setTopic(CommunityConstant.TOPIC_PUBLISH);
            event.setUserId(user.getId());
            event.setEntityType(CommunityConstant.ENTITY_TYPE_POST);
            event.setEntityId(discussPostId);
            eventProducer.fireEvent(event);
        }

        return "redirect:/discussPost/detail/" + discussPostId ;
    }

}

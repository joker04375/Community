package com.serein.community.controller;

import com.serein.community.annotation.LoginRequired;
import com.serein.community.entity.Comment;
import com.serein.community.entity.User;
import com.serein.community.service.CommentService;
import com.serein.community.util.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

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

        return "redirect:/discussPost/detail/" + discussPostId ;
    }

}

package com.serein.community.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class Comment {
    private Long id;
    private Long userId;
    private Long entityId;
    private Long targetId;
    private String content;
    private int entityType;
    private int status;
    private Date createTime;

    //回复评论
    private User user;
    private List<Comment> replyComments = new ArrayList<>();
    private User target;

}

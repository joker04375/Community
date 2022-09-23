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

    // 回复评论
    private User user;
    private List<Comment> replyComments = new ArrayList<>();
    private User target;
    // 对应的文章
    private DiscussPost discussPost;

    // 点赞数量
    private Long likeCount;
    // 点赞状态
    private int likeStatus;

}

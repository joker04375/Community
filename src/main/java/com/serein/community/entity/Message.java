package com.serein.community.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Message {
    private Long id;
    private Long fromId;
    private Long toId;
    private String conversationId;
    private String content;
    private int status;
    private Date createTime;

    // 未读数量与私信数量,当前用户与私信用户目标
    private int letterCount;
    private int unreadCount;

    private User target;
}

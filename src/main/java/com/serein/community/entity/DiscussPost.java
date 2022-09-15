package com.serein.community.entity;


import lombok.Data;

import java.util.Date;

@Data
public class DiscussPost {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Integer type;
    private Integer status;
    private Date createTime;

    private Integer commentCount;
    private Double score;

    private User user;
}

package com.serein.community.entity;


import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private List<Comment> comments = new ArrayList<>();
}

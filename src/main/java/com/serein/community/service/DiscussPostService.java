package com.serein.community.service;

import com.serein.community.entity.DiscussPost;

import java.util.List;

public interface DiscussPostService {
//    List<DiscussPost> selectDiscussPosts(Long userId, int offset, int limit);
    List<DiscussPost> selectDiscussPosts(Long userId);

    int selectDiscussPostRows(Long userId);
}
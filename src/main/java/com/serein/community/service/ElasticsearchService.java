package com.serein.community.service;

import com.serein.community.entity.DiscussPost;

import java.util.List;

public interface ElasticsearchService {
    void deleteDiscussPost(Long id);

    void saveDiscussPost(DiscussPost discussPost);

    List<DiscussPost> searchDiscussPost(String keyword,int current,int limit);

    // 查询结果数量
    long searchDiscussPostTotal(String keyword);
}

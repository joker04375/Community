package com.serein.community.service.impl;

import com.serein.community.entity.DiscussPost;
import com.serein.community.mapper.DiscussPostMapper;
import com.serein.community.service.DiscussPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class DiscussPostServiceImpl implements DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

//    @Override
//    public List<DiscussPost> selectDiscussPosts(Long userId, int offset, int limit) {
//        return discussPostMapper.selectDiscussPosts(userId,offset,limit);
//    }

    @Override
    public List<DiscussPost> selectDiscussPosts(Long userId) {
        return discussPostMapper.selectDiscussPosts(userId);
    }

    @Override
    public int selectDiscussPostRows(Long userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }
}

package com.serein.community.service.impl;

import com.serein.community.entity.DiscussPost;
import com.serein.community.mapper.DiscussPostMapper;
import com.serein.community.service.DiscussPostService;
import com.serein.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
@Service
public class DiscussPostServiceImpl implements DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

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

    @Override
    public int insertDiscussPost(DiscussPost discussPost) {
        if(discussPost == null){
            throw new IllegalArgumentException("参数不能为空");
        }

        // 对标签做处理,转义HTML标记
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        //过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    @Override
    public DiscussPost selectDiscussPostById(Long id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    @Override
    public int updateCommentCount(Long id, int commentCount) {
        return discussPostMapper.updateCommentCount(id,commentCount);
    }
}

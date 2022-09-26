package com.serein.community.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.serein.community.entity.DiscussPost;
import com.serein.community.entity.Page;
import com.serein.community.entity.User;
import com.serein.community.service.ElasticsearchService;
import com.serein.community.service.LikeService;
import com.serein.community.service.UserService;
import com.serein.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SearchController {
    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @GetMapping("/search")
    public String search(String keyword, Model model, Page page){
        // 搜索帖子
        List<DiscussPost> list = elasticsearchService.searchDiscussPost(keyword,page.getCurrent() - 1, page.getLimit());
        if(list != null){
            for (DiscussPost discussPost : list) {
                User user = userService.selectById(discussPost.getUserId());
                discussPost.setUser(user);
                Long likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST, discussPost.getId());
                discussPost.setLikeCount(likeCount);
            }
        }
        model.addAttribute("list",list);
        model.addAttribute("keyword",keyword);
        //分页信息
        page.setPath("/search?keyword=" + keyword);
        long count = elasticsearchService.searchDiscussPostTotal(keyword);
        page.setRows(count == 0 ? 0 : (int) count);

        return "/site/search";
    }
}

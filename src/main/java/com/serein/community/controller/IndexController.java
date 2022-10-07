package com.serein.community.controller;

import com.github.pagehelper.PageInfo;
import com.serein.community.entity.DiscussPost;
import com.serein.community.entity.User;
import com.serein.community.service.DiscussPostService;
import com.serein.community.service.LikeService;
import com.serein.community.service.UserService;
import com.serein.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.github.pagehelper.PageHelper;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class IndexController {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @GetMapping("/index")
    public String getIndexPage(Model model, @RequestParam(defaultValue = "1",value = "pageNum") Integer pageNum){

//        List<Map<String,Object>> discussPosts = new ArrayList<>();
        PageHelper.startPage(pageNum, 10);

        List<DiscussPost> list = discussPostService.selectDiscussPosts(null);

        if(list != null){
            for (DiscussPost discussPost : list) {
                User user = userService.selectById(discussPost.getUserId());
                discussPost.setUser(user);
                Long likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST, discussPost.getId());
                discussPost.setLikeCount(likeCount);
            }
        }

        PageInfo<DiscussPost> pageInfo = new PageInfo<>(list);
        System.out.println("test: "+pageInfo.getList().size());

        model.addAttribute("pageInfo",pageInfo);
        return "/index";
    }

    @GetMapping("/error")
    public String getErrorPage(){
        return "/error/500";
    }

    // 拒绝访问时的提示页面
    @GetMapping("/denied")
    public String getDeniedPage() {
        return "/error/404";
    }
}

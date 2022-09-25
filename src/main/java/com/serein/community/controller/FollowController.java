package com.serein.community.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.serein.community.annotation.LoginRequired;
import com.serein.community.entity.Event;
import com.serein.community.entity.User;
import com.serein.community.event.EventProducer;
import com.serein.community.service.FollowService;
import com.serein.community.service.UserService;
import com.serein.community.util.CommunityConstant;
import com.serein.community.util.CommunityUtil;
import com.serein.community.util.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController {
    @Autowired
    private FollowService followService;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @LoginRequired
    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType,Long entityId){
        User user = UserHolder.getUser();
        followService.follow(user.getId(),entityType,entityId);

        // 触发关注事件
        Event event = new Event();
        event.setTopic(CommunityConstant.TOPIC_FOLLOW);
        event.setUserId(user.getId());
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0,"已关注");
    }

    @LoginRequired
    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityType,Long entityId){
        User user = UserHolder.getUser();
        followService.unFollow(user.getId(),entityType,entityId);

        return CommunityUtil.getJSONString(0,"已取消关注");
    }

    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId")Long userId, Model model,@RequestParam(defaultValue = "1",value = "pageNum") Integer pageNum){
        User user = userService.selectById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);
        List<Map<String, Object>> list = followService.findFollowees(userId);
        for (Map<String, Object> map : list) {
            User u =(User) map.get("user");
            map.put("hasFollowed",hasFollowed(u.getId()));
        }
        PageHelper.startPage(pageNum,5);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);
        model.addAttribute("pageInfo",pageInfo);

        return "/site/followee";
    }


    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId")Long userId, Model model,@RequestParam(defaultValue = "1",value = "pageNum") Integer pageNum){
        User user = userService.selectById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);
        List<Map<String, Object>> list = followService.findFollowers(userId);
        for (Map<String, Object> map : list) {
            User u =(User) map.get("user");
            map.put("hasFollowed",hasFollowed(u.getId()));
        }
        PageHelper.startPage(pageNum,5);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);
        model.addAttribute("pageInfo",pageInfo);

        return "/site/follower";
    }

    private boolean hasFollowed(Long userId){
        User user = UserHolder.getUser();
        if(user==null){
            return false;
        }
        return followService.hasFollowed(user.getId(), CommunityConstant.ENTITY_TYPE_USER,userId);
    }

}



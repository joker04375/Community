package com.serein.community.controller;

import com.serein.community.annotation.LoginRequired;
import com.serein.community.entity.Event;
import com.serein.community.entity.User;
import com.serein.community.event.EventProducer;
import com.serein.community.service.LikeService;
import com.serein.community.util.CommunityConstant;
import com.serein.community.util.CommunityUtil;
import com.serein.community.util.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

@Controller
public class LikeController {
    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @LoginRequired
    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType,Long entityId,Long entityUserId,Long postId){
        User user = UserHolder.getUser();
        // 点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        // 点赞数量
        Long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        // 返回结果
        HashMap<String, Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);

        // 触发点赞事件
        if(likeStatus == 1){
            Event event = new Event();
            event.setTopic(CommunityConstant.TOPIC_LIKE);
            event.setUserId(user.getId());
            event.setEntityType(entityType);
            event.setEntityUserId(entityUserId);
            HashMap<String, Object> data = new HashMap<>();
            event.setData("postId",postId);
            eventProducer.fireEvent(event);
        }

        return CommunityUtil.getJSONString(0,null,map);
    }
}

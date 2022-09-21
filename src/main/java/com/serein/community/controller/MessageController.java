package com.serein.community.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.serein.community.entity.Message;
import com.serein.community.entity.User;
import com.serein.community.service.MessageService;
import com.serein.community.service.UserService;
import com.serein.community.util.CommunityUtil;
import com.serein.community.util.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class MessageController {
    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    // 私信列表
    @GetMapping("/letter/list")
    public String getLetterList(Model model,@RequestParam(defaultValue = "1",value = "pageNum") Integer pageNum){
        PageHelper.startPage(pageNum,5);
        User user = UserHolder.getUser();
        List<Message> messages = messageService.selectConversations(user.getId());
        if(messages!=null){
            for (Message message : messages) {
                message.setLetterCount(messageService.selectLetterCount(message.getConversationId()));
                message.setUnreadCount(messageService.selectLetterUnreadCount(user.getId(),message.getConversationId()));
                Long targetId = user.getId() == message.getFromId()? message.getToId():message.getFromId();
                message.setTarget(userService.selectById(targetId));
            }
        }
        PageInfo<Message> pageInfo = new PageInfo<>(messages);
        model.addAttribute("pageInfo",pageInfo);

        // 查询所有未读消息数量
        int count = messageService.selectLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",count);
        return "/site/letter";
    }

    // 显示私信详情
    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(Model model,@PathVariable("conversationId")String conversationId, @RequestParam(defaultValue = "1",value = "pageNum") Integer pageNum){
        PageHelper.startPage(pageNum,5);
        User user = UserHolder.getUser();
        List<Message> letters = messageService.selectLetters(conversationId);
        if(letters!=null){
            for (Message letter : letters) {
                letter.setTarget(userService.selectById(letter.getFromId()));
            }
        }
        PageInfo<Message> pageInfo = new PageInfo<>(letters);
        model.addAttribute("pageInfo",pageInfo);
        model.addAttribute("conversationId",conversationId);

        //查询私信目标
        model.addAttribute("target",getLetterTarget(conversationId));

        //设置已读
        List<Long> ids = getLetterIds(letters);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    private User getLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");
        Long d0 = Long.parseLong(ids[0]);
        Long d1 = Long.parseLong(ids[1]);
        if(UserHolder.getUser().getId() == d0){
            return userService.selectById(d1);
        }
        return userService.selectById(d0);
    }

    private List<Long> getLetterIds(List<Message> letterList){
        ArrayList<Long> ids = new ArrayList<>();
        if(letterList!=null){
            for (Message message : letterList) {
                // 判断用户为接收者的身份
                if(UserHolder.getUser().getId()==message.getToId()&&message.getStatus()==0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    @PostMapping("/letter/send")
    @ResponseBody
    public String sendLetter(String toName,String content){
        User user = userService.selectByName(toName);
        if(user==null){
            return CommunityUtil.getJSONString(1,"目标用户不存在");
        }
        Message message = new Message();
        message.setFromId(UserHolder.getUser().getId());
        message.setToId(user.getId());
        if(message.getFromId() < message.getToId()){
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        }
        else{
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }

        message.setContent(content);
        message.setStatus(0);
        message.setCreateTime(new Date());
        messageService.insertMessage(message);

        return CommunityUtil.getJSONString(0);
    }

    @PostMapping("/letter/del")
    @ResponseBody
    public String delLetter(Long id){
        if(id>=0){
            List<Long> ids = new ArrayList<>();
            ids.add(id);
            messageService.delMessage(ids);
            return CommunityUtil.getJSONString(0);
        } else {
            return CommunityUtil.getJSONString(1,"数据出错！");
        }
    }
}

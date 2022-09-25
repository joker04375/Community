package com.serein.community.service.impl;

import com.serein.community.entity.Message;
import com.serein.community.mapper.MessageMapper;
import com.serein.community.service.MessageService;
import com.serein.community.util.SensitiveFilter;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Override
    public List<Message> selectConversations(Long userId) {
        return messageMapper.selectConversations(userId);
    }

    @Override
    public int selectConversationCount(Long userId) {
        return messageMapper.selectConversationCount(userId);
    }

    @Override
    public List<Message> selectLetters(String conversationId) {
        return messageMapper.selectLetters(conversationId);
    }

    @Override
    public int selectLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    @Override
    public int selectLetterUnreadCount(Long userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId,conversationId);
    }

    @Override
    public int insertMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    @Override
    public int delMessage(List<Long> ids) {
        return messageMapper.updateStatus(ids,2);
    }

    @Override
    public int readMessage(List<Long> ids) {
        return messageMapper.updateStatus(ids,1);
    }

    @Override
    public Message findLatestNotice(Long userId,String topic){
        return messageMapper.selectLatestNotice(userId,topic);
    }
    @Override
    public int findNoticeCount(Long userId,String topic){
        return messageMapper.selectNoticeCount(userId,topic);
    }
    @Override
    public int findNoticeUnreadCount(Long userId,String topic){
        return messageMapper.selectNoticeUnreadCount(userId,topic);
    }

    @Override
    public List<Message> selectNotices(Long userId, String topic) {
        return messageMapper.selectNotices(userId,topic);
    }
}

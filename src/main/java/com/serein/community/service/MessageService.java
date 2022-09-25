package com.serein.community.service;

import com.serein.community.entity.Message;

import java.util.List;

public interface MessageService {
    List<Message> selectConversations(Long userId);

    // 查询当前用户的会话数量
    int selectConversationCount(Long userId);

    // 查询某个会话所包含的私信列表
    List<Message> selectLetters(String conversationId);

    // 查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);

    // 查询未读私信的数量
    int selectLetterUnreadCount(Long userId,String conversationId);

    // 增加消息
    int insertMessage(Message message);

    // 删除消息
    int delMessage(List<Long> ids);

    // 修改消息为已读状态
    int readMessage(List<Long> ids);

    // 查询最新的系统通知
    Message findLatestNotice(Long userId,String topic);

    int findNoticeCount(Long userId,String topic);

    int findNoticeUnreadCount(Long userId,String topic);

    List<Message> selectNotices(Long userId,String topic);
}

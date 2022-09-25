package com.serein.community.mapper;

import com.serein.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface MessageMapper {
    // 查询当前用户的会话列表，针对每个会话值返回一天最新的私信
    List<Message> selectConversations(Long userId);

    // 查询当前用户的会话数量
    int selectConversationCount(Long userId);

    // 查询某个会话所包含的私信列表
    List<Message> selectLetters(String conversationId);

    // 查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);

    // 查询未读私信的数量
    int selectLetterUnreadCount(@Param("userId") Long userId, @Param("conversationId") String conversationId);

    // 新增消息
    int insertMessage(Message message);

    // 更改消息状态
    int updateStatus(@Param("ids") List<Long> ids,@Param("status") int status);

    // 查询某个主题最新的通知
    Message selectLatestNotice(@Param("userId") Long userId,@Param("topic") String topic);

    // 查询某个主题所包含的通知数量
    int selectNoticeCount(@Param("userId")Long userId,@Param("topic")String topic);

    // 查询未读的通知数量
    int selectNoticeUnreadCount(@Param("userId")Long userId,@Param("topic")String topic);

    // 查询某个主题所包含的通知列表
    List<Message> selectNotices(@Param("userId")Long userId,@Param("topic") String topic);
}

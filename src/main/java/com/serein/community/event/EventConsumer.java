package com.serein.community.event;

import com.alibaba.fastjson.JSONObject;
import com.serein.community.entity.DiscussPost;
import com.serein.community.entity.Event;
import com.serein.community.entity.Message;
import com.serein.community.service.DiscussPostService;
import com.serein.community.service.ElasticsearchService;
import com.serein.community.service.MessageService;
import com.serein.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @KafkaListener(topics = {CommunityConstant.TOPIC_COMMENT,CommunityConstant.TOPIC_LIKE,CommunityConstant.TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record){
        if(record == null || record.value() == null){
            logger.error("消息的内容为空");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error("");
            return;
        }

        // 发送站内通知
        Message message = new Message();
        message.setFromId(CommunityConstant.SYSTEM_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setStatus(0);
        message.setCreateTime(new Date());

        Map<String,Object> content = new HashMap<>();
        // 事件触发者
        content.put("userId",event.getUserId());
        // 实体类型： 评论、帖子、用户
        content.put("entityType",event.getEntityType());
        // 实体id
        content.put("entityId",event.getEntityId());

        // 将事件中的data提取存入到content
        if(!event.getData().isEmpty()){
            for(Map.Entry<String,Object> entry : event.getData().entrySet()){
                content.put(entry.getKey(),entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.insertMessage(message);
    }

    // 消费发帖事件
    @KafkaListener(topics = {CommunityConstant.TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if(record == null || record.value() == null){
            logger.error("消息的内容为空");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error("");
            return;
        }

        DiscussPost discussPost = discussPostService.selectDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(discussPost);
    }
}

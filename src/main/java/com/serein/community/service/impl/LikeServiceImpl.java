package com.serein.community.service.impl;

import com.serein.community.service.LikeService;
import com.serein.community.util.RedisConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeServiceImpl implements LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    // 点赞
    public void like(Long userId,int entityType,Long entityId,Long entityUserId){
//        String entityLikeKey = RedisConstant.getEntityLikeKey(entityType, entityId);
//        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
//        if(isMember){
//            redisTemplate.opsForSet().remove(entityLikeKey,userId);
//        }
//        else{
//            redisTemplate.opsForSet().add(entityLikeKey,userId);
//        }


        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String entityLikeKey = RedisConstant.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisConstant.getUserLikeKey(entityUserId);

                boolean isMember = redisOperations.opsForSet().isMember(entityLikeKey, userId);

                // 开启事务
                redisOperations.multi();

                // 当前用户点赞的同时，更新被点赞用户点赞的数量，否则取消点赞做相反的处理
                if(isMember){
                    redisOperations.opsForSet().remove(entityLikeKey,userId);
                    redisOperations.opsForValue().decrement(userLikeKey);
                }
                else{
                    redisOperations.opsForSet().add(entityLikeKey,userId);
                    redisOperations.opsForValue().increment(userLikeKey);
                }
                // 执行事务
                return redisOperations.exec();
            }
        });
    }

    // 查询实体点赞的数量
    public Long findEntityLikeCount(int entityType,Long entityId){
        String entityLikeKey = RedisConstant.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    // 查询某人对某实体的点赞状态
    public int findEntityLikeStatus(Long userId,int entityType,Long entityId){
        String entityLikeKey = RedisConstant.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId) ? 1 : 0;
    }

    public int findUserLikeCountByUserId(Long userId){
        String userLikeKey = RedisConstant.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }
}

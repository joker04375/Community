package com.serein.community.service.impl;

import com.serein.community.entity.User;
import com.serein.community.service.FollowService;
import com.serein.community.service.UserService;
import com.serein.community.util.CommunityConstant;
import com.serein.community.util.RedisConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowServiceImpl implements FollowService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    // 关注
    public void follow(Long userId,int entityType,Long entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisConstant.getFolloweeKey(userId, entityType);
                String followerKey = RedisConstant.getFollowerKey(entityType, entityId);

                redisOperations.multi();

                // 关注的目标
                redisOperations.opsForZSet().add(followeeKey,entityId,System.currentTimeMillis());
                // 被关注
                redisOperations.opsForZSet().add(followerKey,userId,System.currentTimeMillis());

                return redisOperations.exec();
            }
        });
    }

    // 取关
    public void unFollow(Long userId,int entityType,Long entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisConstant.getFolloweeKey(userId, entityType);
                String followerKey = RedisConstant.getFollowerKey(entityType, entityId);

                redisOperations.multi();

                // 关注的目标
                redisOperations.opsForZSet().remove(followeeKey,entityId);
                // 被关注
                redisOperations.opsForZSet().remove(followerKey,userId);

                return redisOperations.exec();
            }
        });
    }

    // 查询关注的实体的数量
    public Long findFolloweeCount(Long userId,int entityType){
        String followeeKey = RedisConstant.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    // 查询实体的粉丝的数量
    public Long findFollowerCount(int entityType,Long entityId){
        String followerKey = RedisConstant.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    // 查询当前用户是否已关注实体
    public boolean hasFollowed(Long userId,int entityType,Long entityId){
        String followeeKey = RedisConstant.getFolloweeKey(userId, entityType);

        // 有分数说明关注，否则无
        return redisTemplate.opsForZSet().score(followeeKey,entityId) != null;
    }


    // 查询某用户关注的人
    public List<Map<String,Object>> findFollowees(Long userId){
        String followeeKey = RedisConstant.getFolloweeKey(userId, CommunityConstant.ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, 0, -1);
        if(targetIds == null){
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            HashMap<String, Object> map = new HashMap<>();
            User user = userService.selectById(targetId.longValue());
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

    // 查询某用户的粉丝
    public List<Map<String,Object>> findFollowers(Long userId){
        String followerKey = RedisConstant.getFollowerKey(CommunityConstant.ENTITY_TYPE_USER,userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, 0, -1);
        if(targetIds == null){
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();

        for (Integer targetId : targetIds) {
            HashMap<String, Object> map = new HashMap<>();
            User user = userService.selectById(targetId.longValue());
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }
}

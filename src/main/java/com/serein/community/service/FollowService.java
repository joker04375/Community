package com.serein.community.service;

import java.util.List;
import java.util.Map;

public interface FollowService {
    void follow(Long userId,int entityType,Long entityId);

    void unFollow(Long userId,int entityType,Long entityId);

    Long findFolloweeCount(Long userId,int entityType);

    Long findFollowerCount(int entityType,Long entityId);

    boolean hasFollowed(Long userId,int entityType,Long entityId);

    List<Map<String,Object>> findFollowees(Long userId);

    List<Map<String,Object>> findFollowers(Long userId);
}

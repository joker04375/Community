package com.serein.community.service;

public interface LikeService {
    /**
     * 点赞，更新当前用户，以及更新被点赞用户
     * @param userId
     * @param entityType
     * @param entityId
     * @param entityUserId
     */
    void like(Long userId,int entityType,Long entityId,Long entityUserId);

    /**
     * 查询点赞数量
     * @param entityType
     * @param entityId
     * @return
     */
    Long findEntityLikeCount(int entityType,Long entityId);

    /**
     * 查询当前用户的点赞状态
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    int findEntityLikeStatus(Long userId,int entityType,Long entityId);

    /**
     * 根据用户查询收获点赞数量
     * @param userId
     * @return
     */
    int findUserLikeCountByUserId(Long userId);
}

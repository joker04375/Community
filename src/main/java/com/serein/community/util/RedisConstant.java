package com.serein.community.util;

public class RedisConstant {
    private static final String ENTITY_LIKE_KEY = "like:entity";
    private static final String USER_LIKE_KEY = "like:user";
    private static final String FOLLOWEE = "followee";
    private static final String FOLLOWER = "follower";
    private static final String KAPTCHA = "kaptcha";
    private static final String TICKET = "ticket";
    private static final String USER = "user";
    private static final String UV = "uv";
    private static final String DAU = "dau";

    // 某个实体的赞
    // like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType,Long entityId){
        return ENTITY_LIKE_KEY + ":" + entityType + ":" +entityId;
    }

    // 某一个用户的赞
    public static String getUserLikeKey(Long userId){
        return USER_LIKE_KEY + ":" + userId;
    }

    // 某个用户关注的实体
    // followee:userId:entityType -> zset(entityId,now)
    public static String getFolloweeKey(Long userId,int entityType){
        return FOLLOWEE + ":" + userId + ":" + entityType;
    }

    // 某个实体拥有的粉丝
    // flower:entityType:entityId -> zset(userId,now)
    public static String getFollowerKey(int entityType,Long entityId){
        return FOLLOWER + ":" + entityType + ":" + entityId;
    }

    // 登录验证码
    public static String getKaptchaKey(String owner){
        return KAPTCHA + ":" + owner;
    }

    // 登录凭证
    public static String getTicketKey(String ticket){
        return TICKET + ":" +ticket;
    }

    // 登录凭证
    public static String getUserKey(Long userId){
        return USER + ":" + userId;
    }

    // 单日uv
    public static String getUVKey(String date){
        return UV + ":" + date;
    }

    // 区间uv
    public static String getUVKey(String startDate,String endDate){
        return UV + ":" + startDate + ":" + endDate;
    }

    // 单日活跃用户
    public static String getDAUKey(String date){
        return DAU + ":" + date;
    }

    // 区间活跃用户
    public static String getDAUKey(String startDate,String endDate){
        return DAU + ":" + startDate + ":" + endDate;
    }
}

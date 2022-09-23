package com.serein.community.util;

public class CommunityConstant {
    /**
     * 激活成功
     */
    public static final int ACTIVATION_SUCCESS = 1;

    /**
     * 激活成功
     */
    public static final int ACTIVATION_REPEAT = 2;

    /**
     * 激活成功
     */
    public static final int ACTIVATION_FAILED = 3;

    /**
     * 默认状态的登录凭证的超时时间
     */
    public static final int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态的登录凭证的超时时间
     */
    public static final int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;

    /**
     * 评论
     */
    public static final int ENTITY_TYPE_POST = 1;

    /**
     * 回复
     */
    public static final int ENTITY_TYPE_COMMENT = 2;

    /**
     * 用户
     */
    public static final int ENTITY_TYPE_USER = 3;
}

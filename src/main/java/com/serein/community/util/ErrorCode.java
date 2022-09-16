package com.serein.community.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    UNAUTHORIZED(401, "还未授权，不能访问"),
    FORBIDDEN(403, "没有权限，禁止访问"),
    INTERNAL_SERVER_ERROR(500, "服务器异常，请稍后再试"),
    ACCOUNT_ERROR(1001, "账号已被注册"),
    PASSWORD_ERROR(1002, "密码长度不能小于8位"),
    EMAIL_ERROR(1003, "邮箱已被注册");

    private final int code;
    private final String msg;
}

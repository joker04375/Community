package com.serein.community.service;


import com.serein.community.dto.Result;
import com.serein.community.entity.LoginTicket;
import com.serein.community.entity.User;

import java.util.Map;

public interface UserService {
    Result register(User user);

    /**
     * 根据id查询
     * @param id
     * @return
     */
    User selectById(Long id);


    /**
     * 检查激活
     * @param userId
     * @param code
     * @return
     */
    int activation(Long userId,String code);

    /**
     * 登录
     * @param username
     * @param password
     * @param expiredSeconds
     * @return
     */
    Map<String,Object> login(String username, String password, int expiredSeconds);

    /**
     * 登出
     * @param ticket
     */
    void logout(String ticket);

    /**
     * 获取邮箱验证码
     * @param email
     * @return
     */
    Map<String, Object> forgetCode(String email);


    /**
     * 修改密码
     * @param email
     * @param newPassword
     * @return
     */
    Map<String, Object> changePassword(String email, String newPassword);

    /**
     * 查询token
     * @param ticket
     * @return
     */
    LoginTicket findLoginTicketByTicket(String ticket);

    /**
     * 更新用户头像
     * @param userId
     * @param headUrl
     * @return
     */
    int updateHeader(Long userId,String headUrl);


}

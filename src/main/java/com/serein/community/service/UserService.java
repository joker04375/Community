package com.serein.community.service;

import com.serein.community.entity.User;
import org.apache.ibatis.annotations.Update;

public interface UserService {
    /**
     * 根据id查询
     * @param id
     * @return
     */
    User selectById(Long id);

    /**
     * 根据名字查询
     * @param username
     * @return
     */
    User selectByName(String username);

    /**
     * 根据邮箱查询
     * @param email
     * @return
     */
    User selectByEmail(String email);

    /**
     * 添加用户
     * @param user
     * @return
     */
    int insertUser(User user);

    /**
     * 更新用户状态
     * @param id
     * @param status
     * @return
     */
    int updateStatus(Long id, Integer status);

    /**
     * 更新用户的头像
     * @param id
     * @param headerUrl
     * @return
     */
    int updateHeader(Long id, String headerUrl);

    /**
     * 更新用户密码
     * @param id
     * @param password
     * @return
     */
    int updatePassword(Long id, String password);

    /**
     * 根据id删除用户
     * @param id
     * @return
     */
    int deleteById(Long id);
}

package com.serein.community.service;


import com.serein.community.dto.Result;
import com.serein.community.entity.User;

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
    public int activation(Long userId,String code);

}

package com.serein.community.service.impl;

import com.serein.community.entity.User;
import com.serein.community.mapper.UserMapper;
import com.serein.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public User selectById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User selectByName(String username) {
        return userMapper.selectByName(username);
    }

    @Override
    public User selectByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public int insertUser(User user) {
        return userMapper.insertUser(user);
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        return userMapper.updateStatus(id,status);
    }

    @Override
    public int updateHeader(Long id, String headerUrl) {
        return userMapper.updateHeader(id,headerUrl);
    }

    @Override
    public int updatePassword(Long id, String password) {
        return userMapper.updatePassword(id,password);
    }

    @Override
    public int deleteById(Long id) {
        return userMapper.deleteById(id);
    }

}

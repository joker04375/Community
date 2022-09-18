package com.serein.community.mapper;

import com.serein.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserMapper {
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
    int updateStatus(@Param("id")Long id, @Param("status") Integer status);

    /**
     * 更新用户的头像
     * @param id
     * @param headerUrl
     * @return
     */
    int updateHeader(@Param("id") Long id, @Param("headerUrl") String headerUrl);

    /**
     * 更新用户密码
     * @param id
     * @param password
     * @return
     */
    int updatePassword(@Param("id") Long id,@Param("password") String password);

    /**
     * 根据id删除用户
     * @param id
     * @return
     */
    int deleteById(Long id);
}

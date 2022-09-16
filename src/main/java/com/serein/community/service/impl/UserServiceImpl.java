package com.serein.community.service.impl;

import com.serein.community.util.CommunityConstant;
import com.serein.community.util.ErrorCode;
import com.serein.community.dto.Result;
import com.serein.community.entity.User;
import com.serein.community.mapper.UserMapper;
import com.serein.community.service.UserService;
import com.serein.community.util.CommunityUtil;
import com.serein.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public Result register(User user){
        if(user==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            return Result.error("账号不能为空");
        }
        if(StringUtils.isBlank(user.getPassword())){
            return Result.error("密码不能为空");
        }
        if(StringUtils.isBlank(user.getEmail())){
            return Result.error("邮箱不能为空");
        }

        // 验证账号
        User user1 = userMapper.selectByName(user.getUsername());
        if(user1!=null){
            return Result.error(ErrorCode.ACCOUNT_ERROR);
        }

        // 验证密码长度
        if(user.getPassword().length()<8){
            return Result.error(ErrorCode.PASSWORD_ERROR);
        }

        // 验证邮箱
        user1 = userMapper.selectByEmail(user.getEmail());
        if(user1!=null){
            return Result.error(ErrorCode.EMAIL_ERROR);
        }

        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());

        // https://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);

        return Result.ok("注册成功,我们向您的邮箱发生一封激活邮件，请激活您的账号");
    }

    @Override
    public User selectById(Long id) {
        return userMapper.selectById(id);
    }

    public int activation(Long userId,String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus()==1){
            return CommunityConstant.ACTIVATION_REPEAT;
        }
        else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            return CommunityConstant.ACTIVATION_SUCCESS;
        }
        else{
            return CommunityConstant.ACTIVATION_FAILED;
        }
    }
}

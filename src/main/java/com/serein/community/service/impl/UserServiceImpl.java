package com.serein.community.service.impl;

import com.serein.community.entity.LoginTicket;
import com.serein.community.mapper.LoginTicketMapper;
import com.serein.community.util.*;
import com.serein.community.dto.Result;
import com.serein.community.entity.User;
import com.serein.community.mapper.UserMapper;
import com.serein.community.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.serein.community.util.CommunityConstant.*;

@Service
public class UserServiceImpl implements UserService {
//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

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

    @Autowired
    private RedisTemplate redisTemplate;

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
//        return userMapper.selectById(id);
        User user = getCacheUser(id);
        if(user == null){
            user = initCache(id);
        }
        return user;
    }


    public int activation(Long userId,String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus()==1){
            return CommunityConstant.ACTIVATION_REPEAT;
        }
        else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            clearCacheUser(userId);
            return CommunityConstant.ACTIVATION_SUCCESS;
        }
        else{
            return CommunityConstant.ACTIVATION_FAILED;
        }
    }

    public Map<String,Object> login(String username, String password, int expiredSeconds){
        Map<String, Object> map = new HashMap<>();
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        User user = userMapper.selectByName(username);
        if(user==null){
            map.put("usernameMsg","该账号不存在！");
            return map;
        }
        if(user.getStatus() == 0){
            map.put("usernameMsg","该账号未激活！");
            return map;
        }
        password = CommunityUtil.md5(password + user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg","密码不正确！");
            return map;
        }

        //生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000L));
//        loginTicketMapper.insertLoginTicket(loginTicket);

        String redisKey = RedisConstant.getTicketKey(loginTicket.getTicket());
        // 序列化json字符串保存
        redisTemplate.opsForValue().set(redisKey,loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket){
//        loginTicketMapper.updateStatus(ticket,1);
        String redisKey = RedisConstant.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket)redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }

    @Override
    public Map<String, Object> forgetCode(String email) {
        Map<String, Object> map = new HashMap<>();
        // 判断邮箱是否为空
        if(StringUtils.isBlank(email)){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }

        // 判断用户是否存在
        User user = userMapper.selectByEmail(email);
        if(user == null){
            map.put("emailMsg","邮件未注册！请重新检查！");
            return map;
        }

        // 发送邮件
        Context context = new Context();
        context.setVariable("email",email);
        String code = CommunityUtil.generateUUID().substring(0,6);
        context.setVariable("code",code);
        map.put("code",code);

        String content = templateEngine.process("/mail/forget",context);
        mailClient.sendMail(email,"忘记密码",content);

        return map;
    }

    @Override
    public Map<String, Object> changePassword(String email, String newPassword) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        User user = userMapper.selectByEmail(email);
        if(user == null){
            map.put("emailMsg","邮箱错误");
            return map;
        }
        if(newPassword.length()<=6){
            map.put("passwordMsg","密码长度不能小于6位");
            return map;
        }

        userMapper.updatePassword(user.getId(),CommunityUtil.md5(newPassword + user.getSalt()));
        return map;
    }

    @Override
    public LoginTicket findLoginTicketByTicket(String ticket) {
//        return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisConstant.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    @Override
    public int updateHeader(Long userId, String headUrl) {
        int rows = userMapper.updateHeader(userId,headUrl);
        clearCacheUser(userId);
        return rows;
    }

    @Override
    public User selectByName(String name) {
        return userMapper.selectByName(name);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(Long userId) {
        User user = this.selectById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }

    // 1.优先从缓存中读取数据
    public User getCacheUser(Long userId){
        String redisKey = RedisConstant.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 2.取不到时初始化缓存数据
    private User initCache(Long userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisConstant.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }

    // 3.数据变更时清楚缓存数据
    public void clearCacheUser(Long userId){
        String redisKey = RedisConstant.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }
}

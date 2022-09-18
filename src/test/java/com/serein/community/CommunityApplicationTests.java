package com.serein.community;

import com.serein.community.entity.DiscussPost;
import com.serein.community.entity.LoginTicket;
import com.serein.community.entity.User;
import com.serein.community.mapper.DiscussPostMapper;
import com.serein.community.mapper.LoginTicketMapper;
import com.serein.community.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.serein.community.util.MailClient;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.List;

@SpringBootTest
class CommunityApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    void testSelectUserById(){
//        User user = userMapper.selectById(102L);
//        System.out.println(user);
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(null);
        System.out.println(list);
//        for (DiscussPost discussPost : list) {
//            System.out.println(discussPost.getUserId());
//            System.out.println("test:"+discussPost);
//        }
    }

    @Test
    void deleteUser(){
        int i = userMapper.deleteById(102L);
        System.out.println(i);
    }

    @Test
    void testInsertUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123");
        user.setSalt("abc");
        user.setEmail("www.test.com");
        user.setHeaderUrl("/static/img/test.jpg");
        user.setStatus(0);
        user.setCreateTime(new Date());
        int i = userMapper.insertUser(user);
        System.out.println(i);
    }

    @Autowired
    private MailClient mailClient;

    @Test
    public void testSendEmail(){
        mailClient.sendMail("398198945@qq.com","Test","测试");
    }

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testSendEmailHtml(){
        Context context = new Context();
        context.setVariable("username","湘萍");
        String content = templateEngine.process("/mail/threaten", context);
        mailClient.sendMail("1354933501@qq.com","HTML",content);
    }

    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Test
    public void insertTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101L);
        loginTicket.setTicket("abc");
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
        loginTicket.setStatus(0);
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void selectTicket(){
        int abd = loginTicketMapper.updateStatus("abc", 1);
        LoginTicket abc = loginTicketMapper.selectByTicket("abc");
        System.out.println(abc);
    }
}

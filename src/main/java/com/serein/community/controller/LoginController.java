package com.serein.community.controller;

import com.google.code.kaptcha.Producer;
import com.serein.community.util.CommunityConstant;
import com.serein.community.util.CommunityUtil;
import com.serein.community.util.ErrorCode;
import com.serein.community.dto.Result;
import com.serein.community.entity.User;
import com.serein.community.service.UserService;
import com.serein.community.util.RedisConstant;
import com.sun.deploy.net.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Controller
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/register")
    public String getRegisterPage(){
        return "/site/register";
    }

    @GetMapping("/login")
    public String getLoginPage(){
        return "/site/login";
    }


    @PostMapping("/login")
    public String login(String username,String password,String code,boolean rememberMe,
                        Model model,HttpSession session,HttpServletResponse response,
                        @CookieValue("kaptchaOwner")String kaptchaOwner){

//        // 检查验证码
//        String kaptcha = (String)session.getAttribute("kaptcha");
//        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
//            model.addAttribute("codeMsg","验证码不正确！");
//            return "/site/login";
//        }

        // 从redis中取验证码
        String kaptcha = null;
        if(StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey = RedisConstant.getKaptchaKey(kaptchaOwner);
            kaptcha = (String)redisTemplate.opsForValue().get(redisKey);
        }
        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码不正确！");
            return "/site/login";
        }

        // 检查账号，密码
        // 设置超时时间
        int expiredSeconds = rememberMe ? CommunityConstant.REMEMBER_EXPIRED_SECONDS : CommunityConstant.DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if(map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            // 正确
            return "redirect:/index";
        }
        else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));

            // 错误
            return "/site/login";
        }
    }

    @GetMapping("/logout")
    public String logout(@CookieValue("ticket")String ticket){
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

//        // 将验证码存入session
//        session.setAttribute("kaptcha",text);

        // 将验证码存入redis,验证码的归属者
        // 生成用户标识
        String kaptchaOwner = RedisConstant.getKaptchaKey(CommunityUtil.generateUUID());
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // 存入redis
        String redisKey = RedisConstant.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);

        // 将图片输出浏览器
        response.setContentType("/image/png");
        try {
            ServletOutputStream os= response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("响应验证码失败" + e.getMessage());
        }
    }


    @PostMapping("/register")
    public String register(Model model, User user){
        Result result = userService.register(user);
        if(result.getCode()==0){
            model.addAttribute("msg",result.getData());
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }
        if(result.getCode() == ErrorCode.PASSWORD_ERROR.getCode()){
            model.addAttribute("passwordMsg",ErrorCode.PASSWORD_ERROR.getMsg());
        }
        if(result.getCode() == ErrorCode.ACCOUNT_ERROR.getCode()){
            model.addAttribute("accountMsg",ErrorCode.ACCOUNT_ERROR.getMsg());
        }
        if(result.getCode() == ErrorCode.EMAIL_ERROR.getCode()){
            model.addAttribute("emailMsg",ErrorCode.EMAIL_ERROR.getMsg());
        }

        return "/site/register";
    }



    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") Long userId,@PathVariable("code") String code){
        int activation = userService.activation(userId, code);
        if(activation== CommunityConstant.ACTIVATION_SUCCESS){
            model.addAttribute("msg", "激活成功，您的账号可以正常使用了");
            model.addAttribute("target","/index");
        }
        else if(activation==CommunityConstant.ACTIVATION_REPEAT){
            model.addAttribute("msg", "无效操作，账号已激活");
            model.addAttribute("target","/index");
        }
        else{
            model.addAttribute("msg", "激活失败，激活码不正确");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    @GetMapping("/forget")
    public String getForgetPage() { return "/site/forget";}

    @RequestMapping(path = "/forget/code",method = RequestMethod.GET)
    public String getForgetCode(String email,Model model,HttpSession session){
        Map<String,Object> map = userService.forgetCode(email);
        // 错误
        if(map.containsKey("emailMsg")){
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/forget";
        }
        else{
            session.setAttribute("forgetCode",map.get("code"));

            model.addAttribute("emailMsg","邮件已发送");
            logger.info("邮件已发送");
            logger.info("修改密码-验证码为" + map.get("code"));
        }
        return "/site/forget";
    }

    @PostMapping("/forget")
    public String changeForgetPassWord(String email,String code,String newPassword,
                                       Model model,HttpSession session){
        String forgetCode = session.getAttribute("forgetCode").toString();

        // 判断验证码是否正确
        if(StringUtils.isBlank(forgetCode)){
            logger.error("没有验证码");
            model.addAttribute("codeMsg","没有验证码！请先获取验证码");
            return "/site/forget";
        }
        if(StringUtils.isBlank(code)){
            model.addAttribute("codeMsg","验证码不能为空，请重新输入");
            return "/site/forget";
        }
        if(!forgetCode.equals(code)){
            model.addAttribute("codeMsg","验证码错误，请检查");
            return "/site/forget";
        }

        // 修改密码
        Map<String,Object> map = userService.changePassword(email,newPassword);
        // 判断是否修改成功
        if(map.containsKey("emailMsg")){
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/forget";
        }
        if(map.containsKey("passwordMsg")){
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/forget";
        }

        logger.info("修改密码成功");
        model.addAttribute("msg","修改密码成功，请尝试登陆");
        model.addAttribute("target","/login");

        return "/site/operate-result";
    }
}

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

//        // ???????????????
//        String kaptcha = (String)session.getAttribute("kaptcha");
//        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
//            model.addAttribute("codeMsg","?????????????????????");
//            return "/site/login";
//        }

        // ???redis???????????????
        String kaptcha = null;
        if(StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey = RedisConstant.getKaptchaKey(kaptchaOwner);
            kaptcha = (String)redisTemplate.opsForValue().get(redisKey);
        }
        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","?????????????????????");
            return "/site/login";
        }

        // ?????????????????????
        // ??????????????????
        int expiredSeconds = rememberMe ? CommunityConstant.REMEMBER_EXPIRED_SECONDS : CommunityConstant.DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if(map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            // ??????
            return "redirect:/index";
        }
        else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));

            // ??????
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
        // ???????????????
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

//        // ??????????????????session
//        session.setAttribute("kaptcha",text);

        // ??????????????????redis,?????????????????????
        // ??????????????????
        String kaptchaOwner = RedisConstant.getKaptchaKey(CommunityUtil.generateUUID());
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // ??????redis
        String redisKey = RedisConstant.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);

        // ????????????????????????
        response.setContentType("/image/png");
        try {
            ServletOutputStream os= response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("?????????????????????" + e.getMessage());
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
            model.addAttribute("msg", "????????????????????????????????????????????????");
            model.addAttribute("target","/index");
        }
        else if(activation==CommunityConstant.ACTIVATION_REPEAT){
            model.addAttribute("msg", "??????????????????????????????");
            model.addAttribute("target","/index");
        }
        else{
            model.addAttribute("msg", "?????????????????????????????????");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    @GetMapping("/forget")
    public String getForgetPage() { return "/site/forget";}

    @RequestMapping(path = "/forget/code",method = RequestMethod.GET)
    public String getForgetCode(String email,Model model,HttpSession session){
        Map<String,Object> map = userService.forgetCode(email);
        // ??????
        if(map.containsKey("emailMsg")){
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/forget";
        }
        else{
            session.setAttribute("forgetCode",map.get("code"));

            model.addAttribute("emailMsg","???????????????");
            logger.info("???????????????");
            logger.info("????????????-????????????" + map.get("code"));
        }
        return "/site/forget";
    }

    @PostMapping("/forget")
    public String changeForgetPassWord(String email,String code,String newPassword,
                                       Model model,HttpSession session){
        String forgetCode = session.getAttribute("forgetCode").toString();

        // ???????????????????????????
        if(StringUtils.isBlank(forgetCode)){
            logger.error("???????????????");
            model.addAttribute("codeMsg","???????????????????????????????????????");
            return "/site/forget";
        }
        if(StringUtils.isBlank(code)){
            model.addAttribute("codeMsg","???????????????????????????????????????");
            return "/site/forget";
        }
        if(!forgetCode.equals(code)){
            model.addAttribute("codeMsg","???????????????????????????");
            return "/site/forget";
        }

        // ????????????
        Map<String,Object> map = userService.changePassword(email,newPassword);
        // ????????????????????????
        if(map.containsKey("emailMsg")){
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/forget";
        }
        if(map.containsKey("passwordMsg")){
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/forget";
        }

        logger.info("??????????????????");
        model.addAttribute("msg","????????????????????????????????????");
        model.addAttribute("target","/login");

        return "/site/operate-result";
    }
}

package com.serein.community.controller;

import com.serein.community.util.CommunityConstant;
import com.serein.community.util.ErrorCode;
import com.serein.community.dto.Result;
import com.serein.community.entity.User;
import com.serein.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String getRegisterPage(){
        return "/site/register";
    }

    @GetMapping("/login")
    public String getLoginPage(){
        return "/site/login";
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
}

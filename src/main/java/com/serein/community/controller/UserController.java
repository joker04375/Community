package com.serein.community.controller;

import com.github.pagehelper.util.StringUtil;
import com.serein.community.annotation.LoginRequired;
import com.serein.community.entity.User;
import com.serein.community.mapper.UserMapper;
import com.serein.community.service.UserService;
import com.serein.community.util.CommunityUtil;
import com.serein.community.util.UserHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Controller
@RequestMapping("/user")
public class UserController {

    public static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Value("${community.path.upload}")
    private String upLoadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserMapper userMapper;

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage(){
        return "/site/setting";
    }

    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage==null){
            model.addAttribute("error","您还没有选择文件");
            return "/site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确");
            return "/site/setting";
        }

        // 生成随机文件名
        filename = CommunityUtil.generateUUID() + suffix;

        // 确定文件存放的路径
        File file = new File(upLoadPath + "/" + filename);
        try {
            // 存储文件
            headerImage.transferTo(file);
        } catch (IOException e) {
            logger.error("上传头像文件失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常");
        }

        // 更新当前用户头像的路径(web访问路径)
        // http://localhost:8080/community/user/header/xxx.png
        User user = UserHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }

    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        // 服务器存放路径
        fileName = upLoadPath + "/" + fileName;

        // 解析文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));

        // 响应图片
        response.setContentType("image/" + suffix);
        try(
                ServletOutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(fileName);
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fis.read(buffer)) != -1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }

    @LoginRequired
    @PostMapping( "/settingPassword")
    public String settingPassword(String oldPassword,String newPassword
            ,Model model,@CookieValue("ticket") String ticket){
        User user = UserHolder.getUser();
        if(user == null){
            return "/site/login";
        }

        if(user.getPassword().equals(CommunityUtil.md5(oldPassword+user.getSalt()))){
            // 原密码输入正确
            if(newPassword.length() < 6){
                model.addAttribute("newPasswordMsg","新密码长度不能小于6位");
                return "/site/setting";
            }
            userService.changePassword(user.getEmail(),newPassword);
            userService.logout(ticket);
            model.addAttribute("msg","修改密码成功，请尝试登陆");
            model.addAttribute("target","/login");
            return "/site/operate-result";
        }
        else {
            model.addAttribute("oldPasswordMsg","原密码输入不正确");
            return "/site/setting";
        }
    }

}

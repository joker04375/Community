package com.serein.community.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.serein.community.annotation.LoginRequired;
import com.serein.community.entity.Comment;
import com.serein.community.entity.DiscussPost;
import com.serein.community.entity.User;
import com.serein.community.service.*;
import com.serein.community.util.CommunityConstant;
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
import java.util.List;

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
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

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


    // 个人主页 个人信息
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId")Long userId,Model model){
        User user = userService.selectById(userId);
        if(user==null){
            throw new IllegalArgumentException("用户不存在");
        }

        // 用户
        model.addAttribute("user",user);

        // 点赞数量
        int likeCount = likeService.findUserLikeCountByUserId(userId);
        model.addAttribute("likeCount",likeCount);

        // 关注数量
        Long followeeCount = followService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);

        // 粉丝数量
        Long followerCount = followService.findFollowerCount(CommunityConstant.ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);

        // 是否已关注
        boolean hasFollowed = false;
        if(UserHolder.getUser()!=null){
            hasFollowed = followService.hasFollowed(UserHolder.getUser().getId(), CommunityConstant.ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        return "/site/profile";
    }

    // 个人主页 我的帖子
    @GetMapping("/myPost/{userId}")
    public String getMyPostPage(@PathVariable("userId")Long userId,Model model, @RequestParam(defaultValue = "1",value = "pageNum") Integer pageNum){
        User user = userService.selectById(userId);
        PageHelper.startPage(pageNum,5);
        List<DiscussPost> list = discussPostService.selectDiscussPosts(userId);
        if(list!=null){
            for (DiscussPost discussPost : list) {
                Long likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST, discussPost.getId());
                discussPost.setLikeCount(likeCount);
            }
        }

        PageInfo<DiscussPost> pageInfo = new PageInfo<>(list);

        model.addAttribute("pageInfo",pageInfo);
        model.addAttribute("user",user);

        return "/site/my-post";
    }

    // 个人主页 我的回复
    @GetMapping("/myReply/{userId}")
    public String getMyReplyPage(@PathVariable("userId")Long userId,Model model, @RequestParam(defaultValue = "1",value = "pageNum") Integer pageNum){
        User user = userService.selectById(userId);
        PageHelper.startPage(pageNum,5);
        List<Comment> list = commentService.findReplyByUser(userId);
        if(list!=null){
            for (Comment comment : list) {
                DiscussPost discussPost = discussPostService.selectDiscussPostById(comment.getEntityId());
                comment.setDiscussPost(discussPost);
            }
        }

        PageInfo<Comment> pageInfo = new PageInfo<>(list);

        model.addAttribute("pageInfo",pageInfo);
        model.addAttribute("user",user);

        return "/site/my-reply";
    }
}

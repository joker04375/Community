package com.serein.community.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.serein.community.dto.Result;
import com.serein.community.entity.Comment;
import com.serein.community.entity.DiscussPost;
import com.serein.community.entity.Event;
import com.serein.community.entity.User;
import com.serein.community.event.EventProducer;
import com.serein.community.service.CommentService;
import com.serein.community.service.DiscussPostService;
import com.serein.community.service.LikeService;
import com.serein.community.service.UserService;
import com.serein.community.util.CommunityConstant;
import com.serein.community.util.CommunityUtil;
import com.serein.community.util.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/discussPost")
public class DiscussPostController {
    @Autowired
    private CommentService commentService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user = UserHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(403,"你还没有登录");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUser(user);
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setType(0);
        discussPost.setStatus(0);
        discussPost.setCreateTime(new Date());
        discussPostService.insertDiscussPost(discussPost);

        // 触发发帖事件
        Event event = new Event();
        event.setTopic(CommunityConstant.TOPIC_PUBLISH);
        event.setUserId(user.getId());
        event.setEntityType(CommunityConstant.ENTITY_TYPE_POST);
        event.setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);

        // 报错的情况将来统一处理
        return CommunityUtil.getJSONString(0,"发布成功");
    }

    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") Long discussPostId, Model model,@RequestParam(defaultValue = "1", value = "pageNum") Integer pageNum){
        // 查询帖子
        DiscussPost discussPost = discussPostService.selectDiscussPostById(discussPostId);

        // 查询作者
        User user = userService.selectById(discussPost.getUserId());
        discussPost.setUser(user);

        // 点赞数量
        Long LikeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST, discussPostId);
        discussPost.setLikeCount(LikeCount);

        // 点赞状态
        int likeStatus;
        if(user==null){
            likeStatus = 0;
        }
        likeStatus = likeService.findEntityLikeStatus(user.getId(),CommunityConstant.ENTITY_TYPE_POST,discussPostId);

        model.addAttribute("likeStatus",likeStatus);
        model.addAttribute("discussPost",discussPost);


        PageHelper.startPage(pageNum,5);
        List<Comment> comments = commentService.selectCommentsByEntity(CommunityConstant.ENTITY_TYPE_POST, discussPost.getId());

        PageInfo<Comment> pageInfo = new PageInfo<>(comments);
        if(pageInfo!=null){
            for (Comment comment : pageInfo.getList()) {
                // 评论
                // 评论作者
                comment.setUser(userService.selectById(comment.getUserId()));
                List<Comment> replyList = commentService.selectCommentsByEntity(CommunityConstant.ENTITY_TYPE_COMMENT, comment.getId());

                // 点赞数量
                Long LikeCount1 = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_COMMENT, comment.getId());
                comment.setLikeCount(LikeCount1);

                // 点赞状态
                int likeStatus1;
                if(user == null){
                    likeStatus1 = 0;
                }
                likeStatus1 = likeService.findEntityLikeStatus(user.getId(),CommunityConstant.ENTITY_TYPE_COMMENT,comment.getId());
                comment.setLikeStatus(likeStatus1);

                for (Comment reply : replyList) {
                    // 回复
                    // 回复的目标
                    reply.setUser(userService.selectById(reply.getUserId()));
                    User target = reply.getTargetId() == 0
                            ? null
                            : userService.selectById(reply.getTargetId());
                    reply.setTarget(target);

                    // 点赞数量
                    Long LikeCount2 = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_COMMENT, reply.getId());
                    comment.setLikeCount(LikeCount2);

                    // 点赞状态
                    int likeStatus2;
                    if(user == null){
                        likeStatus2 = 0;
                    }
                    likeStatus2 = likeService.findEntityLikeStatus(user.getId(),CommunityConstant.ENTITY_TYPE_COMMENT,reply.getId());
                    comment.setLikeStatus(likeStatus2);
                }
                comment.setReplyComments(replyList);
            }
            discussPost.setComments(comments);
        }

        model.addAttribute("pageInfo",pageInfo);

        return "/site/discuss-detail";
    }


}

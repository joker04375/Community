package com.serein.community.advice;

import com.serein.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

// 只去扫描带有Controller注解的组件
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws  IOException{
        logger.error("服务器发送异常" + e.getMessage());

        // 记录堆栈异常
        for(StackTraceElement element : e.getStackTrace()){
            logger.error(element.toString());
        }

        // 获取请求方式
        String requestWith = request.getHeader("x-request-with");


        if("XMLHttpRequest".equals(requestWith)){
            // 异步请求

            // 普通字符串
            response.setContentType("application/plain");

//            // json字符串
//            response.setContentType("application/json");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1,"服务器异常"));
        }
        else{
            //不是异步请求
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}

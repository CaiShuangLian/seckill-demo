package com.csl.seckill.controller;

import com.csl.seckill.service.IUserService;
import com.csl.seckill.vo.LoginVo;
import com.csl.seckill.vo.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author:CaiShuangLian
 * @FileName:
 * @Date:Created in  2021/9/9 17:29
 * @Version:
 * @Description:登录
 */

@Controller
@RequestMapping("/login")
@Slf4j
public class LoginController {

    @Autowired
    IUserService userService;

    @RequestMapping("/toLogin")
    public String toLogin(){
        return "login";
    }

    @RequestMapping("/doLogin")
    @ResponseBody
    public RespBean doLogin(LoginVo loginVo){
        log.info("{}",loginVo);//使用的是Lombok的@Slf4j
        return userService.doLogin(loginVo);//具体逻辑在service层实现
//        return null;
    }
}

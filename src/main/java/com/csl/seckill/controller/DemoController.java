package com.csl.seckill.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author:CaiShuangLian
 * @FileName:
 * @Date:Created in  2021/9/9 9:47
 * @Version:
 * @Description:TODO
 */

@Controller
@RequestMapping("/demo")
public class DemoController {

    /**
     * 测试页面跳转
     * @param model
     * @return
     */
    @RequestMapping("/hello")
    public String hello(Model model){
        model.addAttribute("name","csl");
        return "hello";
    }
}

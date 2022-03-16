package com.csl.seckill.controller;

import com.csl.seckill.pojo.User;
import com.csl.seckill.service.IGoodsService;
import com.csl.seckill.service.IUserService;
import com.csl.seckill.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @Author:CaiShuangLian
 * @FileName:
 * @Date:Created in  2021/9/11 16:09
 * @Version:
 * @Description:商品
 */

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    IUserService userService;
    @Autowired
    private IGoodsService goodsService;
    //引入Redis
    @Autowired
    private RedisTemplate redisTemplate;
    //做手动渲染
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;
//    @RequestMapping("/toList")
//    public String toList(HttpServletRequest request, HttpServletResponse response, Model model, @CookieValue("userTicket") String ticket){
//        if(StringUtils.isEmpty(ticket)){
//            return "login";
//        }
////        User user=(User)session.getAttribute(ticket);
//        User user =userService.getUserByCookie(ticket,request,response);
//        if(null==user){
//            return "login";
//        }
//        model.addAttribute("user",user);
//        return "goodsList";
//    }

    /**
     * 跳转到商品列表
     * @param model
     * @param user
     * @return
     */
//    @RequestMapping("/toList")
//    public String toList(Model model, User user){
//        model.addAttribute("user",user);
//        model.addAttribute("goodList",goodsService.findGoodsVo());
//        return "goodsList";
//    }

    /**
     * 跳转商品列表 redis缓存
     * @param model
     * @param user
     * @return
     */
    @RequestMapping(value = "/toList",produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toList(Model model, User user,
                         HttpServletRequest request, HttpServletResponse response){
        //Redis中获取页面，如果不为空，直接返回页面
        ValueOperations valueOperations=redisTemplate.opsForValue();
        String html=(String)valueOperations.get("goodsList");
        if(!StringUtils.isEmpty(html)){
            return html;
        }


        model.addAttribute("user",user);
        model.addAttribute("goodList",goodsService.findGoodsVo());
        //return “goodsList"

        //如果为空，手动渲染，存入Redis并返回
        WebContext webContext = new WebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap());
        html=thymeleafViewResolver.getTemplateEngine().process("goodsList",webContext);
        if(!StringUtils.isEmpty(html)){
            valueOperations.set("goodsList",html,60, TimeUnit.SECONDS);
        }
        return html;
    }

    /**
     * 根据商品id得到商品详情
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
//    @RequestMapping("/toDetail/{goodsId}")
//    public String toDetail(Model model,User user, @PathVariable Long goodsId){
//        model.addAttribute("user",user);
//        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
//        Date startDate=goodsVo.getStartDate();
//        Date endDate=goodsVo.getEndDate();
//        Date nowDate=new Date();
//        //秒杀状态
//        int seckillStatus=0;
//        int remainSeconds=0;
////        判断状态
//        if(nowDate.before(startDate)){
//            //秒杀倒计时
//            remainSeconds= (int) ((startDate.getTime()-nowDate.getTime())/1000);
//        }else if(nowDate.after(endDate)){
//            //秒杀已结束
//            seckillStatus=2;
//            remainSeconds=-1;
//        }else {
//            //秒杀进行中
//            seckillStatus=1;
//            remainSeconds=0;
//        }
//        model.addAttribute("remainSeconds",remainSeconds);
//        model.addAttribute("seckillStatus",seckillStatus);
//        model.addAttribute("goods",goodsVo);
//        return "goodsDetail";
//    }
    @RequestMapping(value = "/toDetail/{goodsId}",produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toDetail(Model model,User user, @PathVariable Long goodsId,
                           HttpServletRequest request,HttpServletResponse response){
        ValueOperations valueOperations=redisTemplate.opsForValue();
        //Redis中获取页面，如果不为空，直接返回页面
        String html=(String)valueOperations.get("goodsDetail:"+goodsId);
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        model.addAttribute("user",user);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate=goodsVo.getStartDate();
        Date endDate=goodsVo.getEndDate();
        Date nowDate=new Date();
        //秒杀状态
        int seckillStatus=0;
        int remainSeconds=0;
//        判断状态
        if(nowDate.before(startDate)){
            //秒杀倒计时
            remainSeconds= (int) ((startDate.getTime()-nowDate.getTime())/1000);
        }else if(nowDate.after(endDate)){
            //秒杀已结束
            seckillStatus=2;
            remainSeconds=-1;
        }else {
            //秒杀进行中
            seckillStatus=1;
            remainSeconds=0;
        }
        model.addAttribute("remainSeconds",remainSeconds);
        model.addAttribute("seckillStatus",seckillStatus);
        model.addAttribute("goods",goodsVo);
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(),model.asMap());
        html=thymeleafViewResolver.getTemplateEngine().process("goodsDetail",webContext);
        if(!StringUtils.isEmpty(html)){
            valueOperations.set("goodsDetail:"+goodsId,html,60,TimeUnit.SECONDS);
        }
        return html;
    }


}


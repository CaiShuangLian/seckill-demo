package com.csl.seckill.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.csl.seckill.pojo.Order;
import com.csl.seckill.pojo.SeckillOrder;
import com.csl.seckill.pojo.User;
import com.csl.seckill.service.IGoodsService;
import com.csl.seckill.service.IOrderService;
import com.csl.seckill.service.ISeckillOrderService;
import com.csl.seckill.vo.GoodsVo;
import com.csl.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author:CaiShuangLian
 * @FileName:
 * @Date:Created in  2021/9/17 9:49
 * @Version:
 * @Description:秒杀
 */

@Controller
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IOrderService orderService;

    @RequestMapping("/doSeckill")
    public String doSeckill(Model model, User user, Long goodsId){

        if(user==null)
        {
            return "login";
        }
        model.addAttribute("user",user);
        GoodsVo goods  = goodsService.findGoodsVoByGoodsId(goodsId);
        //判断库存
        if(goods.getGoodsCount()<1){
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "seckillFail";
        }
        //判断是否重复抢购
        //MybatisPlus的一个写法
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
        if(seckillOrder!=null){
            model.addAttribute("errmsg",RespBeanEnum.REPEATE_ERROR.getMessage());
            return "seckillFail";
        }
        Order order=orderService.seckill(user,goods);
        model.addAttribute("order",order);
        model.addAttribute("goods",goods);
        return "orderDetail";

    }
}


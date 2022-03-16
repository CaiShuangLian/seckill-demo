package com.csl.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.csl.seckill.pojo.Order;
import com.csl.seckill.pojo.User;
import com.csl.seckill.vo.GoodsVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author CaiShuangLian
 * @since 2021-09-15
 */
public interface IOrderService extends IService<Order> {

    /**
     * 秒杀
     * @param user
     * @param goods
     * @return
     */
    Order seckill(User user, GoodsVo goods);
}

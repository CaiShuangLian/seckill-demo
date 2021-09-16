package com.csl.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.csl.seckill.mapper.OrderMapper;
import com.csl.seckill.pojo.Order;
import com.csl.seckill.service.IOrderService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author CaiShuangLian
 * @since 2021-09-15
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

}

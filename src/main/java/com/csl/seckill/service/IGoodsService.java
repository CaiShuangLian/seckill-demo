package com.csl.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.csl.seckill.pojo.Goods;
import com.csl.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author CaiShuangLian
 * @since 2021-09-15
 */
public interface IGoodsService extends IService<Goods> {

    /**
     * 获取商品列表
     * @return
     */
    List<GoodsVo> findGoodsVo();

    /**
     * 获取商品详情
     * @param goodsId
     * @return
     */
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}

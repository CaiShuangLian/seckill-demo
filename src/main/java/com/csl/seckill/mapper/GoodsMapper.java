package com.csl.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csl.seckill.pojo.Goods;
import com.csl.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author CaiShuangLian
 * @since 2021-09-15
 */
public interface GoodsMapper extends BaseMapper<Goods> {

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

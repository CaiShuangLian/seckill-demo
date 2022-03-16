package com.csl.seckill.vo;

import com.csl.seckill.pojo.Goods;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author:CaiShuangLian
 * @FileName:
 * @Date:Created in  2021/9/15 19:05
 * @Version:
 * @Description:商品返回对象
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsVo extends Goods {

    private BigDecimal seckillPrice;
//    private Integer stockCount;
    private Integer goodsCount;
    private Date startDate;
    private Date endDate;
}

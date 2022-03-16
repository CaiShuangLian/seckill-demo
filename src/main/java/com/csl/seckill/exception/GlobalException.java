package com.csl.seckill.exception;

import com.csl.seckill.vo.RespBeanEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author:CaiShuangLian
 * @FileName:
 * @Date:Created in  2021/9/11 10:38
 * @Version:
 * @Description:全局异常
 */

@Data
@NoArgsConstructor      //空参构造
@AllArgsConstructor     //全参构造
public class GlobalException extends RuntimeException{
    private RespBeanEnum respBeanEnum;
}


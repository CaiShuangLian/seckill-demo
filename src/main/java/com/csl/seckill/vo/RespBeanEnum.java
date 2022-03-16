package com.csl.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @Author:CaiShuangLian
 * @FileName:
 * @Date:Created in  2021/9/9 17:47
 * @Version:
 * @Description:公共返回对象枚举
 */
@Getter
@ToString
@AllArgsConstructor
public enum  RespBeanEnum {
//    通用
    SUCCESS(200,"SUCCESS"),
    ERROR(500,"服务端异常"),
//    登录
    LOGIN_ERROR(500210,"用户名或密码错误！"),
    MOBILE_ERROR(500211,"手机号码格式不正确"),

//    异常
    BIN_ERROR(500212,"参数校验异常"),

//    秒杀模块5005XX
    EMPTY_STOCK(500500,"库存不足"),
    REPEATE_ERROR(500501,"每人限购一件")
    ;

    private final Integer code;
    private final String message;
}

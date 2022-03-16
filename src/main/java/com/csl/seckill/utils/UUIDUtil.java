package com.csl.seckill.utils;

import java.util.UUID;

/**
 * @Author:CaiShuangLian
 * @FileName:
 * @Date:Created in  2021/9/11 11:31
 * @Version:
 * @Description:生成cookie工具类
 */

public class UUIDUtil {
     public static String uuid() {
            return UUID.randomUUID().toString().replace("-", "");
     }
}

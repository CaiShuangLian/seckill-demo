package com.csl.seckill.utils;

import org.thymeleaf.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author:CaiShuangLian
 * @FileName:
 * @Date:Created in  2021/9/9 18:51
 * @Version:
 * @Description:手机号校验工具类
 */

public class ValidatorUtil {
    private static final Pattern mobile_pattern=Pattern.compile("[1]([3-9])[0-9]{9}$");//正则表达式

    public static boolean isMobile(String mobile){
        if(StringUtils.isEmpty(mobile))
            return false;
        Matcher matcher=mobile_pattern.matcher(mobile);
        return matcher.matches();
    }
}

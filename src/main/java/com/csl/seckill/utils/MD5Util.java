package com.csl.seckill.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

/**
 * @Author:CaiShuangLian
 * @FileName:
 * @Date:Created in  2021/9/9 11:23
 * @Version:
 * @Description:MD5工具类
 *
 */

@Component
public class MD5Util {

    /**
     * 将数据进行MD5加密
     * @param src
     * @return
     */
    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }

    private static final String salt="1a2b3c4d";

    /**
     * 第一次MD5加密（即将前台输入的数据加密后传到后台）
     * @param inputPass
     * @return
     */
    public static String inputPassToFormPass(String inputPass){
        //为了安全性，取部分盐值
        String str=""+salt.charAt(0)+salt.charAt(2)+inputPass+salt.charAt(5)+salt.charAt(4);

        return md5(str);
    }

    /**
     * 第二次MD5加密（将后台的数据加密后传到数据库）
     * @param formPass
     * @param salt
     * @return
     */
    public static String formPassToDBPass(String formPass,String salt){
        String str=""+salt.charAt(0)+salt.charAt(2)+formPass+salt.charAt(5)+salt.charAt(4);
        return md5(str);
    }

    /**
     * 将输入的数据进行两次加密后传到数据库
     * @param inputPass
     * @param salt
     * @return
     */
    public static String inputPassToDBPass(String inputPass,String salt){
        String formPass=inputPassToFormPass(inputPass);
        String dbPass=formPassToDBPass(formPass,salt);
        return dbPass;
    }

    /**
     * 测试
     * @param args
     */
    public static void main(String[] args) {
//        d3b1294a61a07da9b49b6e22b2cbd7f9
        System.out.println(inputPassToFormPass("123456"));
//        b640b74f58005bb70b1c963a025b7549
        System.out.println(formPassToDBPass("d3b1294a61a07da9b49b6e22b2cbd7f9","1a2b3c4d"));
//       b640b74f58005bb70b1c963a025b7549
        System.out.println(inputPassToDBPass("123456","1a2b3c4d"));
    }
}

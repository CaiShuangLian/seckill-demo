package com.csl.seckill.vo;

import com.csl.seckill.validator.IsMobile;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @Author:CaiShuangLian
 * @FileName:
 * @Date:Created in  2021/9/9 18:24
 * @Version:
 * @Description:登录参数
 */

@Data
public class LoginVo {
    @NotNull
    @IsMobile(required = true,message = "手机号码格式错误")
    private String mobile;

    @NotNull
    @Length(min = 32)
    private String password;
}

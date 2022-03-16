package com.csl.seckill.vo;

import com.csl.seckill.utils.ValidatorUtil;
import com.csl.seckill.validator.IsMobile;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @Author:CaiShuangLian
 * @FileName:
 * @Date:Created in  2021/9/11 9:43
 * @Version:
 * @Description:手机号码校验规则
 */

public class IsMobileValidator implements ConstraintValidator<IsMobile,String> {

    //获取手机号是否为必填项
    private boolean required=false;

    @Override
    public void initialize(IsMobile constraintAnnotation) {
        required=constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        //手机号是否是必填项
        if (required) {
            if (StringUtils.isEmpty(value)) {
                return false;//空值无效
            } else
                return ValidatorUtil.isMobile(value);//判断手机号是否正确
        }
        //手机号不是必填项则直接返回true，表示输入的数据有效
        else {
            return true;
        }
    }
}

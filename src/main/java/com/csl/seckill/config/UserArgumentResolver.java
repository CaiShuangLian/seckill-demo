package com.csl.seckill.config;

import com.csl.seckill.pojo.User;
import com.csl.seckill.service.IUserService;
import com.csl.seckill.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author:CaiShuangLian
 * @FileName:
 * @Date:Created in  2021/9/15 17:09
 * @Version:
 * @Description:自定义用户参数
 */
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private IUserService userService;

    /**
     * 判断获取的参数是否是User
     * @param parameter
     * @return
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> clazz=parameter.getParameterType();
        return clazz== User.class;
    }

    /**
     * 是User，进行下一步的判断
     * @param parameter
     * @param mavContainer
     * @param webRequest
     * @param binderFactory
     * @return
     * @throws Exception
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request=webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response=webRequest.getNativeResponse(HttpServletResponse.class);
        String ticket= CookieUtil.getCookieValue(request,"userTicket");
        if(StringUtils.isEmpty(ticket)){
            return null;
        }
        return userService.getUserByCookie(ticket,request,response);
    }
}

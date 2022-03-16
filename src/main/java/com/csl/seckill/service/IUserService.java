package com.csl.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.csl.seckill.pojo.User;
import com.csl.seckill.vo.LoginVo;
import com.csl.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author CaiShuangLian
 * @since 2021-09-09
 */
public interface IUserService extends IService<User> {

    /**
     * 登录
     * @param loginVo
     * @param request
     * @param response
     * @return
     */
    RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    /**
     * 根据cookie获取用户
     * @param userTicket
     * @return
     */
    User getUserByCookie(String userTicket,HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse);
}

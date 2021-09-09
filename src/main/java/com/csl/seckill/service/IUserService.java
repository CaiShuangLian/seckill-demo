package com.csl.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.csl.seckill.pojo.User;
import com.csl.seckill.vo.LoginVo;
import com.csl.seckill.vo.RespBean;

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
     * @return
     */
    RespBean doLogin(LoginVo loginVo);
}

package com.csl.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.csl.seckill.mapper.UserMapper;
import com.csl.seckill.pojo.User;
import com.csl.seckill.service.IUserService;
import com.csl.seckill.utils.MD5Util;
import com.csl.seckill.utils.ValidatorUtil;
import com.csl.seckill.vo.LoginVo;
import com.csl.seckill.vo.RespBean;
import com.csl.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author CaiShuangLian
 * @since 2021-09-09
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 登录功能
     * @param loginVo
     * @return
     */
    @Override
    public RespBean doLogin(LoginVo loginVo) {
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        //判断用户名或者密码是否为空
        if(StringUtils.isEmpty(mobile)||StringUtils.isEmpty(password)){
            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }
        //判断手机号输入是否正确
        if(!ValidatorUtil.isMobile(mobile)){
            return RespBean.error(RespBeanEnum.MOBILE_ERROR);
        }
        //根据手机号获取用户
        User user = userMapper.selectById(mobile);
        if(user==null)
            return RespBean.error(RespBeanEnum.LOGIN_ERROR);

        //判断密码是否正确
        if(!MD5Util.formPassToDBPass(password,user.getSalt()).equals(user.getPwd())){
            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }
        return RespBean.success();
    }
}
package com.csl.seckill.exception;

import com.csl.seckill.vo.RespBean;
import com.csl.seckill.vo.RespBeanEnum;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;




/**
 * @Author:CaiShuangLian
 * @FileName:
 * @Date:Created in  2021/9/11 10:42
 * @Version:
 * @Description:TODO
 */

@RestControllerAdvice	//统一异常处理
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public RespBean ExceptionHandler(Exception e){
        if(e instanceof GlobalException){
            GlobalException ex=(GlobalException)e;
            return RespBean.error(ex.getRespBeanEnum());
        }
        //在登录时产生的参数校验异常是BindException异常
        else if(e instanceof BindException){
            BindException ex=(BindException)e;
            RespBean respBean = RespBean.error(RespBeanEnum.BIN_ERROR);
            //具体异常信息从控制台中得知
            respBean.setMessage("参数校验异常:"+ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
            return respBean;
        }
        return RespBean.error(RespBeanEnum.ERROR);
    }

}

package com.gupao.mvc.aop;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 获取代理对象的原始对象
 */
public class GPAopProxyUtils {

    public  static  Object getTargetObject(Object proxy)throws Exception{

        //先判断是不是代理对象，不是代理对象，就直接返回，是的话，走下面的逻辑
        if(!isAopProxy(proxy)){
            return proxy;
        }
        return getProxyTargetObject(proxy);
    }

    /**
     * 判断是不是代理对象
     * @param obj
     */
    private static boolean isAopProxy(Object obj){

        return Proxy.isProxyClass(obj.getClass());
    }

    private static Object getProxyTargetObject(Object proxy)throws Exception{
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        GPAopProxy aopProxy = (GPAopProxy) h.get(proxy);

        Field target = aopProxy.getClass().getDeclaredField("target");
        target.setAccessible(true);

        return target.get(aopProxy);
    }
}

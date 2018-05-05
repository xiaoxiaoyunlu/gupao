package com.gupao.mvc.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 动态代理类
 * 本次默认使用jdk动态代理
 * 可以自己添加 cglib 动态代理
 */
public class GPAopProxy implements InvocationHandler {

    private GPAopConfig config;

    //要代理的目标类
    private  Object target;

    /**
     * 把原生的对象传进来
     * @param instance
     * @return
     */
    public Object getProxy(Object instance){
        //保存一下原生对象
        this.target=instance;
        Class<?> clazz = instance.getClass();
        //jdk里面会判断，是否实现接口，实现接口，则用jdk动态代理
        //没有接口，就用cglib 动态代理
        Object obj = Proxy.newProxyInstance(clazz.getClassLoader(), clazz
                .getInterfaces(), this);
        return obj;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //因为所有需要aop的method 其实都是切入了before after

        // 调用之前 before
        if(config.contains(method)){
            GPAopConfig.GPAspect aspect=config.get(method);
            aspect.getPoints()[0].invoke(aspect.getAspect());
        }

        //调用方法
        Object result = method.invoke(this.target, args);

        // 调用之后  after
        if(config.contains(method)){
            GPAopConfig.GPAspect aspect=config.get(method);
            aspect.getPoints()[1].invoke(aspect.getAspect());
        }


        return result;
    }

    public void setConfig(GPAopConfig config){
        this.config=config;
    }
}

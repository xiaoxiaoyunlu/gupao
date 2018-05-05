package com.gupao.mvc.beans;

import com.gupao.mvc.aop.GPAopConfig;
import com.gupao.mvc.aop.GPAopProxy;
import com.gupao.mvc.core.GPFactoryBean;

public class GPBeanWrapper extends GPFactoryBean {

    private GPAopProxy proxy=new GPAopProxy();

    // 还会用到观察者设计者模式？
    //1、 支持事件响应 会有一个监听？
    private GPBeanPostProcessor postProcessor;
    //通过原始的反射new出来的对象，要包装一下存起来
    private  Object originalInstance;

    private  Object wrapperInstance;

    public GPBeanWrapper(Object instance) {
        //开始的做法是 直接返回原始对象
        //从这里开始改造，真正返回动态代理对象
        this.originalInstance = instance;
        this.wrapperInstance = proxy.getProxy(instance);
    }

    public GPBeanPostProcessor getPostProcessor() {
        return postProcessor;
    }

    public void setPostProcessor(GPBeanPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

    public Object getWrapperInstance(){
        return this.wrapperInstance;
    }

    public Class<?> getWrappedClass(){
        return this.wrapperInstance.getClass();
    }

    public void setAopConfig(GPAopConfig config){
        this.proxy.setConfig(config);
    }
}

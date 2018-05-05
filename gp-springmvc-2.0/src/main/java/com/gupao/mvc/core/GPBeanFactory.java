package com.gupao.mvc.core;

public interface GPBeanFactory {

    /**
     * 根据beanName 从IOC容器中获取一个bean
     * @param beanName
     * @return
     */
    public Object getBean(String beanName);
}

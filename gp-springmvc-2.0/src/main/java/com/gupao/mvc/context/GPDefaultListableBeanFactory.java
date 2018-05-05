package com.gupao.mvc.context;

import com.gupao.mvc.beans.GPBeanDefinition;
import org.springframework.beans.BeansException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class GPDefaultListableBeanFactory extends GPAbstractApplicationContext {

    //用来保存配置信息
    protected Map<String,GPBeanDefinition> beanDefinitionMap=new
            ConcurrentHashMap<>();

    /**
     * 留给子类重写的
     * @throws BeansException
     */
    protected void refresh() throws BeansException {

    }

    @Override
    protected void refreshBeanFactroy() throws Exception {

    }
}

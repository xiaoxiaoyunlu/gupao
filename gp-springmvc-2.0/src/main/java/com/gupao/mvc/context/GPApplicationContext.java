package com.gupao.mvc.context;

import com.gupao.mvc.annotation.GPAutoWired;
import com.gupao.mvc.annotation.GPController;
import com.gupao.mvc.annotation.GPService;
import com.gupao.mvc.aop.GPAopConfig;
import com.gupao.mvc.beans.GPBeanDefinition;
import com.gupao.mvc.beans.GPBeanPostProcessor;
import com.gupao.mvc.beans.GPBeanWrapper;
import com.gupao.mvc.core.GPBeanFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 听2.0 里面的修改
 */
public class GPApplicationContext extends GPDefaultListableBeanFactory implements GPBeanFactory {

    private String[] configLocations;

    private GPBeanDefinitionReader reader;

    //用来保证注册时单实例的容器
    private  Map<String,Object> beanCacheMap=new HashMap<>();

    //用来存储所有被代理过得对象
    private Map<String,GPBeanWrapper> beanWrapperMap = new ConcurrentHashMap<String, GPBeanWrapper>();


    public GPApplicationContext(String ... configLocations){
        this.configLocations = configLocations;
        refresh();
    }

    @Override
    public void refresh()throws BeansException {
        //定位
        this.reader=new GPBeanDefinitionReader(configLocations);
        //加载
        List<String> beanDefinitions = reader.loadBeanDefinitions();
        //注册
        doRegistry(beanDefinitions);

//        reader.getConfig().getProperty("");

        //依赖注入（lazy-init = false），要是执行依赖注入
        //在这里自动调用getBean方法
        doAutoWired();

    }

    /**
     * Ioc 容器 自动注入
     */
    private void doAutoWired() {
        for(Map.Entry<String,GPBeanDefinition> beanDefinitionEntry : this.beanDefinitionMap.entrySet()){
            String beanName = beanDefinitionEntry.getKey();

            if(!beanDefinitionEntry.getValue().isLazyInit()){
                Object bean = getBean(beanName);
                System.out.println(bean.getClass());
            }

        }

        for(Map.Entry<String,GPBeanWrapper> beanWrapperEntry : this.beanWrapperMap.entrySet()){

            populateBean(beanWrapperEntry.getKey(),beanWrapperEntry.getValue().getWrapperInstance());

        }

    }

    /**
     * 真正的将BeanDefinitions注册到beanDefinitionMap中
     * @param beanDefinitions
     */
    private void doRegistry(List<String> beanDefinitions) {
        //beanName有三种情况:
        //1、默认是类名首字母小写
        //2、自定义名字
        //3、接口注入

        try {
            for (String className:beanDefinitions) {
                Class<?> beanClass = Class.forName(className);
                //如果是一个接口，是不能实例化的
                //用它实现类来实例化
                if(beanClass.isInterface()){continue;}

                GPBeanDefinition beanDefinition = reader.registerBean(className);
                if(beanDefinition!=null){
                    this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
                }

                Class<?>[] interfaces = beanClass.getInterfaces();
                for (Class<?> i:interfaces) {
                    //如果是多个实现类，只能覆盖
                    //为什么？因为Spring没那么智能，就是这么傻
                    //这个时候，可以自定义名字
                    this.beanDefinitionMap.put(i.getName(),beanDefinition);
                }

                //到这里为止，容器初始化完毕

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public int getBeanDefinitionCount() {
//        return getBeanFactory().getBeanDefinitionCount();
        return this.beanDefinitionMap.size();
    }

    public String[] getBeanDefinitionNames() {
//        return getBeanFactory().getBeanDefinitionNames();
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    /**
     * 首字母小写
     * @param beanName
     * @return
     */
    private String lowerFirstChar(String beanName) {
        char[] chars=beanName.toCharArray();
        chars[0]+=32;
        return String.valueOf(chars);
    }

    /**
     * 依赖注入，从这里开始 通过读取BeanDefiniton中的信息
     * 然后，通过反射创建一个实例并返回
     * Spring的做法是  不会返回一个原始的对象出去，而是用会一个BeanWrapper进行一次包装
     *
     * 装饰器模式：
     * 1、保留原来的OOP关系
     * 2、需要对他进行扩展，增强（为了以后AOP打基础）
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object getBean(String beanName) throws BeansException {
        Object instance=null;
        GPBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        String className = beanDefinition.getBeanClassName();

        try {
            //生成通知事件
            GPBeanPostProcessor beanPostProcessor=new GPBeanPostProcessor();
            instance=instationBean(beanDefinition);
            if(null==instance){return instance;}

            //在实例化之前调用一次
            beanPostProcessor.postProcessBeforeInitialization(instance,beanName);
            GPBeanWrapper beanWrapper=new GPBeanWrapper(instance);
            beanWrapper.setPostProcessor(beanPostProcessor);
            //在这里可以拿到原生对象 和  原生对象方法  已经原始的配置
            beanWrapper.setAopConfig(instantionAopConfig(beanDefinition));
            this.beanWrapperMap.put(beanName,beanWrapper);
            //在实例化之后调用一次
            beanPostProcessor.postProcessAfterInitialization(instance,beanName);

            //这样调用一来回，给了我们可操作空间来扩展
//          populateBean(beanName,instance);
            return this.beanWrapperMap.get(beanName).getWrapperInstance();


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


    public void populateBean(String beanName, Object instance) {
        Class<?> clazz = instance.getClass();
        //不是所有牛奶都叫特仑苏
        if(!(clazz.isAnnotationPresent(GPController.class) ||
                clazz.isAnnotationPresent(GPService.class))){
            return;
        }

        Field[] fields = clazz.getDeclaredFields();


            for (Field field:fields) {
                if(!field.isAnnotationPresent(GPAutoWired.class)){continue;}
                GPAutoWired gpAutoWired=field.getAnnotation(GPAutoWired.class);
                String autowiredBeanName = gpAutoWired.value().trim();
                if("".equals(autowiredBeanName)){
                    autowiredBeanName=field.getType().getName();
                }
                //设置强制访问
                field.setAccessible(true);

                try {
                    field.set(instance,this.beanWrapperMap.get(autowiredBeanName).getWrapperInstance());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }


            }

    }

    /**
     * 传一个BeanDefinition，就返回一个实例Bean
     * @param beanDefinition
     * @return
     */
    private Object instationBean(GPBeanDefinition beanDefinition) {
        Object instance=null;

        String className = beanDefinition.getBeanClassName();

        try {
            //因为根据Class才能确定一个类是否有实例
            if(this.beanCacheMap.containsKey(className)){
                instance=beanCacheMap.get(className);
            }else{
                Class<?> clazz = Class.forName(className);
                instance=clazz.newInstance();
                this.beanCacheMap.put(className,instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return instance;
        }
    }





    public Properties getConfig(){
        return this.reader.getConfig();
    }

    private GPAopConfig instantionAopConfig(GPBeanDefinition beanDefinition) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        GPAopConfig aopConfig=new GPAopConfig();
        Properties config = reader.getConfig();
        String pointCut = config.getProperty("pointCut");
        String[] aspectBefores = config.getProperty("aspectBefore").split("\\s");
        String[] aspectAfters = config.getProperty("aspectAfter").split("\\s");

        //获取目标代理对象
        String className = beanDefinition.getBeanClassName();
        Class<?> clazz = Class.forName(className);

        //正则封装切面规则expression
        Pattern pattern=Pattern.compile(pointCut);

        //获取切面的  aspect   LogAspect
        Class<?> aspectClass = Class.forName(aspectAfters[0]);

        for(Method m:clazz.getMethods()){

            Matcher matcher=pattern.matcher(m.toString());
           if(matcher.matches()){
               //能满足切面规则的类，添加到配Aop配置中
               aopConfig.put(m,aspectClass.newInstance(),new
                       Method[]{aspectClass.getMethod(aspectBefores[1]),
                      aspectClass.getMethod(aspectAfters[1])});
           }
        }

        return aopConfig;
    }
}

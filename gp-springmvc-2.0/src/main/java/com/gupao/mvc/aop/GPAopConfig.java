package com.gupao.mvc.aop;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 封装 切面的配置信息  只是对expression的配置封装
 * 目标代理对象的一个方法需要增强
 * 由自己的业务逻辑去实现增强
 *
 *
 *
 */
public class GPAopConfig {

    //以目标对象需要增强的方法method作为key
    //以需要增强的代码内容作为value
   private Map<Method,GPAspect> points=new HashMap<>();


   public void put(Method target,Object aspect,Method[] points){
       this.points.put(target,new GPAspect(aspect,points));
   }

   public GPAspect get(Method method){
       return  this.points.get(method);
   }

   public boolean contains(Method method){
       return this.points.containsKey(method);
   }


    /**
     * 对增强的代码的封装
     */
   class GPAspect{
       //待会将LogAspect这个对象赋值给它
       private  Object aspect;
       //待会会将LogAspect的before和 after 方法赋值进来
       private Method[] points;

       public GPAspect(Object aspect, Method[] points) {
           this.aspect = aspect;
           this.points = points;
       }

       public Object getAspect() {
           return aspect;
       }

       public void setAspect(Object aspect) {
           this.aspect = aspect;
       }

       public Method[] getPoints() {
           return points;
       }

       public void setPoints(Method[] points) {
           this.points = points;
       }
   }

}

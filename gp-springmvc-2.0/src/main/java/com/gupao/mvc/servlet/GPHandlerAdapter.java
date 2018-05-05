package com.gupao.mvc.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

public class GPHandlerAdapter {

    private Map<String,Integer> paramMapping;

    public GPHandlerAdapter(Map<String,Integer> paramMapping){
        this.paramMapping = paramMapping;
    }

    public GPModelAndView handler(HttpServletRequest req, HttpServletResponse resp,GPHandlerMapping handler) throws InvocationTargetException, IllegalAccessException {

        //根据用户传进来的参数信息。跟method中的参数信息进行动态匹配
        //resp传进来的目的只有一个 那即是将其赋值给方法参数？

        //只有当用户传过来的ModelAndView为空是，才会new

        // 1、要准备好这个方法的形参列表
        //方法重载：形参的决定因素：参数个数，参数类型，参数顺序，方法的名字
        Class<?>[] parameTypes = handler.getMethod().getParameterTypes();

        //2、要拿到自定义命名参数所在的位置
        Map<String, String[]> parameterMap = req.getParameterMap();
        //3、构造实参列表
        Object[] paramValues = new Object[parameTypes.length];

        for(Map.Entry<String,String[]> param:parameterMap.entrySet()){
            String value=Arrays.toString(param.getValue()).replaceAll("\\[|\\]","").replaceAll("\\s","");
            if(!this.paramMapping.containsKey(param.getKey())){ continue;}
            int index = paramMapping.get(param.getKey());
            //因为页面上传过来的值都是string类型的，而方法中定义的都是千变万化的
            //因此要对我们传过来的参数进行类型转换
            // spring 定义了很多转换器，根据类型选择合适的转换器处理
            //并且 springmvc可以把参数封装为一个对象处理 怎么实现的？
            paramValues[index]=caseStringValue(value,parameTypes[index]);

        }
        //处理非命名参数，request和  response
        //int reqIndex = this.paramMapping.get(HttpServletRequest.class.getName());
//        Integer reqIndex = this.paramMapping.get(HttpServletRequest.class.getName());
//        if(null!=reqIndex){
        if(this.paramMapping.containsKey(HttpServletRequest.class.getName())){
            int reqIndex = this.paramMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex]=req;
        }

        if(this.paramMapping.containsKey(HttpServletResponse.class.getName())){
            int respIndex=this.paramMapping.get(HttpServletResponse.class.getName());
            paramValues[respIndex]=resp;
        }




        // 4、从 handler 中取出 controller 和  mehthod 然后利用反射机制调用
        Object result = handler.getMethod().invoke(handler.getController(), paramValues);
        if(null==result){
            return  null;
        }
        boolean isModelAndView = handler.getMethod().getReturnType() == GPModelAndView.class;

        if(isModelAndView){
            return (GPModelAndView) result;
        }else{
            return null;
        }

    }

    public Object caseStringValue(String value,Class<?> type){
        if(type==String.class){
            return value;
        }else if(type==Integer.class){
            return Integer.valueOf(value);
        }else if(type==Long.class){
            return Long.valueOf(value);
        }else if(type==int.class){
            return  Integer.valueOf(value).intValue();
        }else if(type==long.class){
            return Long.valueOf(value).longValue();
        }else{
            return null;
        }
    }
}

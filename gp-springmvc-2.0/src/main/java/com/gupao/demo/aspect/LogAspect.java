package com.gupao.demo.aspect;

/**
 * 定义切面
 *
 */
public class LogAspect {

    /**
     * 方法调用之前，调用切入
     */
    public void before(){

        System.out.printf("print log before method invoke");
    }

    /**
     * 方法调用之后，调用切入
     */
    public void after(){
        System.out.printf("print log after method invoke");
    }

}

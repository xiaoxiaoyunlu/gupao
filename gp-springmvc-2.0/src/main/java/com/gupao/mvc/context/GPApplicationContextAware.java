package com.gupao.mvc.context;

public interface GPApplicationContextAware {

    void setApplicationContext(GPApplicationContext applicationContext) throws
            Exception;
}

package com.gupao.mvc.context;

import org.springframework.beans.BeansException;

public abstract  class GPAbstractApplicationContext {

    protected  abstract void refreshBeanFactroy()throws Exception;
}

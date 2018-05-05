package com.gupao.demo.action;

import com.gupao.demo.service.IDemoService;
import com.gupao.mvc.annotation.GPAutoWired;
import com.gupao.mvc.annotation.GPController;
import com.gupao.mvc.annotation.GPRequestMapping;
import com.gupao.mvc.annotation.GPRequestParam;
import com.gupao.mvc.servlet.GPModelAndView;

import java.util.HashMap;
import java.util.Map;

@GPController
@GPRequestMapping("/page")
public class PageAction {

    @GPAutoWired
    private IDemoService demoService;

    @GPRequestMapping("/first.html")
    public GPModelAndView query(@GPRequestParam("teacher") String teacher){
        String result = demoService.query(teacher);
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("teacher", teacher);
        model.put("data", result);
        model.put("token", "123456");
        return new GPModelAndView("first.html",model);
    }
}

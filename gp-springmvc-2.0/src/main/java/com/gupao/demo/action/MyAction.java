package com.gupao.demo.action;


import com.gupao.demo.service.IDemoService;
import com.gupao.mvc.annotation.GPAutoWired;
import com.gupao.mvc.annotation.GPController;
import com.gupao.mvc.annotation.GPRequestMapping;
import com.gupao.mvc.annotation.GPRequestParam;

@GPController
@GPRequestMapping("/my")
public class MyAction {

    @GPAutoWired
    private IDemoService demoService;

    @GPRequestMapping("/qury.json")
    public  void query(){

    }
}

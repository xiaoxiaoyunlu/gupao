package com.gupao.demo.action;

import com.gupao.demo.service.IDemoService;
import com.gupao.mvc.annotation.GPAutoWired;
import com.gupao.mvc.annotation.GPController;
import com.gupao.mvc.annotation.GPRequestMapping;
import com.gupao.mvc.annotation.GPRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@GPController
@GPRequestMapping("/web")
public class DemoAction {
    @GPAutoWired
    private IDemoService demoService;

    @GPRequestMapping("/query.json")
    public void query(HttpServletRequest req, HttpServletResponse resp, @GPRequestParam("name")  String name){
        String result=demoService.get(name);
        try {
            resp.getWriter().write(result);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @GPRequestMapping("/edit")
    public void edit(HttpServletRequest req,HttpServletResponse resp,@GPRequestParam("id") Integer id){

    }
}

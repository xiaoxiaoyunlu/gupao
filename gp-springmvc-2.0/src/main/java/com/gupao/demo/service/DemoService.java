package com.gupao.demo.service;

import com.gupao.mvc.annotation.GPService;

@GPService
public class DemoService implements  IDemoService {
    @Override
    public String get(String name) {
        return "My name is "+name;
    }

    @Override
    public String query(String teacher) {
        return teacher;
    }
}

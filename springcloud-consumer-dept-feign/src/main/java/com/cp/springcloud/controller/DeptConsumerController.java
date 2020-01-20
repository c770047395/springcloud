package com.cp.springcloud.controller;

import com.cp.springcloud.pojo.Dept;
import com.cp.springcloud.service.DeptClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
public class DeptConsumerController {

    @Autowired
    private DeptClientService service;

    @RequestMapping("consumer/dept/get/{id}")
    public Dept get(@PathVariable("id") Long id){
        return this.service.queryById(id);
    }

    @RequestMapping("consumer/dept/add")
    public boolean add(Dept dept){
        System.out.println(dept);
        return this.service.addDept(dept);
    }

    @RequestMapping("consumer/dept/list")
    public List<Dept> list(){
        return this.service.queryAll();
    }

    @RequestMapping("/dept/discovery")
    public Object discovery(){
        return this.service.dis();
    }
}

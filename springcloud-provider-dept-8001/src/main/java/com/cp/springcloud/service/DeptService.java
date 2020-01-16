package com.cp.springcloud.service;

import com.cp.springcloud.pojo.Dept;

import java.util.List;
public interface DeptService {
    public boolean addDept(Dept dept);
    public Dept queryById(Long id);
    public List<Dept> queryAll();
}

package com.leechao.didi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leechao.didi.entity.Employee;
import com.leechao.didi.mapper.EmployeeMapper;
import com.leechao.didi.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

}

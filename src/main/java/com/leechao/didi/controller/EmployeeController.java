package com.leechao.didi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leechao.didi.common.R;
import com.leechao.didi.entity.Employee;
import com.leechao.didi.service.EmployeeService;
import com.leechao.didi.service.impl.EmployeeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录controller
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee>login(HttpServletRequest request,@RequestBody Employee employee){//@RequestBody将前端传入的json对象封装成Employee对象
        //1、将页面提交的密码进行MD5加密
        String password = employee.getPassword();//页面提交的密码已经封装到employee对象中，通过解析这个对象就可获得需要的账号密码
        password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));

        //2、根据页面提交的账号查询数据库
        LambdaQueryWrapper<Employee>queryWrapper = new LambdaQueryWrapper<>();//包装一个查询对象
        queryWrapper.eq(Employee::getUsername,employee.getUsername());//添加一个查询条件(等值查询)
        Employee emp = employeeService.getOne(queryWrapper);

        //3、如果没有查询到结果则返回失败结果
        if(emp==null){
            return R.error("登录失败");
        }

        //4、进行密码比对
        if(!emp.getPassword().equals(password)){//如果数据库中查询到的密码和前端页面传过来的密码不一致
            return R.error("登录失败");
        }

        //5、查看员工状态。如果已为禁用状态，则返回员工已禁用的结果
        if(emp.getStatus()==0){//状态码为0说明已经禁用
            return R.error("账号已禁用");
        }

        //6、登录成功，将员工的id放入到session中，并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出controller
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){//需要操作Session，所以需要将HttpServletRequest作为参数传入
        //清理Session中保存的当前的登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工controller
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工：{}",employee.toString());

        //设置初始密码123456，需要进行MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));

        //设置创建员工时间
        //employee.setCreateTime(LocalDateTime.now());//获取当前系统时间

        //设置更新信息时间
        //employee.setUpdateTime(LocalDateTime.now());

        //获取当前登录用户的id，并设置到employee属性中
        //Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setCreateUser(empId);
        //employee.setUpdateUser(empId);

        employeeService.save(employee);//实现了mybatis-plus的Iservice接口，通过接口中的save()方法将employee存入到数据库中
        return R.success("新增员工成功");
    }

    /**
     * 员工信息分页查询controller
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page>page(int page,int pageSize,String name){//Page是Mybatisplus框架中的一个类，用来做分页查询
        log.info("page={},pageSize={},name={}",page,pageSize,name);
        //1、构造分页构造器
        Page pageInfo=new Page(page,pageSize);

        //2、构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper();
        //添加一个过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加一个排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        Long id=Thread.currentThread().getId();
        log.info("线程id为：{}",id);

        //3、执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public R<String>update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());
        /**
        前面登录功能中实现了将登录是将用户的id传入session中
            登录成功，将员工的id放入到session中，并返回登录成功结果
            request.getSession().setAttribute("employee",emp.getId());
         */
        Long empID = (Long) request.getSession().getAttribute("employee");//通过session中取到了当前操作的用户
        employee.setUpdateTime(LocalDateTime.now());//设置当前时间为跟新时间
        employee.setUpdateUser(empID);

        Long id=Thread.currentThread().getId();
        log.info("线程id为：{}",id);

        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    /**
     * 根据id查询员工信息controller
     * @param id
     * @return
     */
    /*
    @PathVariable主要作用：映射URL绑定的占位符
    带占位符的URL是 Spring3.0 新增的功能，URL中的 {xxx} 占位符可以通过
    @PathVariable("xxx") 绑定到操作方法的入参中。
     */
    @GetMapping("/{id}")
    public R<Employee>getById(@PathVariable Long id){
        log.info("根据id查询员工信息");
        Employee employee = employeeService.getById(id);
        if(employee!=null){
            return R.success(employee);
        }
        return R.error("没有查询到对应员工的信息");
    }
}
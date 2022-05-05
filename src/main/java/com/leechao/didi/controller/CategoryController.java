package com.leechao.didi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leechao.didi.common.R;
import com.leechao.didi.entity.Category;
import com.leechao.didi.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理
 */
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类controller
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("category:{}",category);
        categoryService.save(category);
        return R.success("新增分类成功！");
    }

    /**
     * 分页查询controller
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page>page(int page,int pageSize){
        //分页构造器
        Page<Category>pageInfo=new Page<>(page,pageSize);
        //构造一个条件构造器
        LambdaQueryWrapper<Category>queryWrapper=new LambdaQueryWrapper<>();
        //根据sort字段，添加一个排序条件
        queryWrapper.orderByAsc(Category::getSort);
        //进行分页查询
        categoryService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 根据id删除分类controller
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String>delete(Long ids){
        log.info("删除分类，id为：{}",ids);
        //categoryService.removeById(ids);
        categoryService.remove(ids);
        return R.success("分类信息删除成功");
    }

    /**
     * 实现修改功能
     * @param category
     * @return
     */
    @PutMapping
    public R<String>updata(@RequestBody Category category){
        log.info("修改分类信息：{}",category);
        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }

    /**
     * 根据条件查询分类数据
     * @param category
     * @return
     */
    @GetMapping("/list")
    /**get请求和@RequestBody的关系
     * @RequestBody的作用：
     * 该注解用于读取 Request 请求的 body 部分数据，使用系统默认配置的 HttpMessageConverter 进行解析，然后把相应的数据绑定到要返回的对象上；
     * 再把 HttpMessageConverter 返回的对象数据绑定到 controller 中方法的参数上。
            一般GET请求的参数为实体对象的时候不用@RequestBody也能请求成功
     */
    public R<List<Category>> list(Category category){//此处不添加 @Requestbody注解，原因：
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());//先添加一个条件

        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);//优先使用第一个排序，第一个排序相等的前提下使用第二个排序
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
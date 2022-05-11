package com.leechao.didi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leechao.didi.common.R;
import com.leechao.didi.dto.SetmealDto;
import com.leechao.didi.entity.Category;
import com.leechao.didi.entity.Setmeal;
import com.leechao.didi.service.CategoryService;
import com.leechao.didi.service.SetmealDishService;
import com.leechao.didi.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;
    /**
     * 新增套餐controller
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)//表示清除setmealCache这个类中的所有缓存数据
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息:{}",setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功！");
    }

    /**
     * 套餐分页查询controller
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page>page(int page,int pageSize,String name){
        //1、分页构造器对象
        Page<Setmeal>pageInfo=new Page<>(page,pageSize);
        Page<SetmealDto>dtoPage=new Page<>();

        //条件构造器
        LambdaQueryWrapper<Setmeal>queryWrapper=new LambdaQueryWrapper<>();

        //添加查询条件,根据前端传过来的name进行模糊查询
        queryWrapper.like(name!=null,Setmeal::getName,name);
        //添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");

        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto>list=records.stream().map((item)->{//采用stream流的形式遍历records
            SetmealDto setmealDto = new SetmealDto();
            //对象拷贝
            BeanUtils.copyProperties(item,setmealDto);//将除了CategoryName外的其他属性拷贝
            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询分类对象
            Category category = categoryService.getById(categoryId);//拿到了分类对象
            if(category!=null){
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    /**
     * 删除套餐controller
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)//表示清除setmealCache这个类中的所有缓存数据
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        setmealService.removeWithDish(ids);

        return R.success("套餐数据删除成功！");
    }

    /**
     * 移动端查询套餐信息controller
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    //@Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId + '_' + #setmeal.status")
    public R<List<Setmeal>>list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal>queryWrapper=new LambdaQueryWrapper<>();
        //等值查询，条件为CategoryId
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }
}
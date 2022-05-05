package com.leechao.didi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leechao.didi.common.CustomException;
import com.leechao.didi.entity.Category;
import com.leechao.didi.entity.Dish;
import com.leechao.didi.entity.Setmeal;
import com.leechao.didi.mapper.CategoryMapper;
import com.leechao.didi.service.CategoryService;
import com.leechao.didi.service.DishService;
import com.leechao.didi.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;
    /**
     * 自定义方法，根据id删除分类，删除之前需要进行判断是否关联了菜品和套餐
     * @param id
     */
    @Override
    public void remove(Long id) {
        //1、先查询当前分类是否关联了菜品，如果关联，则抛出一个业务异常
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        //添加查询条件，根据分类id进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int count1 = dishService.count(dishLambdaQueryWrapper);
        if(count1>0){
            //说明此时该分类已经关联了菜品，需要抛出业务异常类
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        //2、查询当前分类是否关联了套餐，如果关联，则抛出一个业务异常
        LambdaQueryWrapper<Setmeal>setmealLambdaQueryWrapper=new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        if(count2>0){
            //说明此时该分类已经关联了套餐，需要抛出业务异常类
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }

        //3、都没有关联，则正常删除
        super.removeById(id);
    }
}

package com.leechao.didi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leechao.didi.common.CustomException;
import com.leechao.didi.dto.SetmealDto;
import com.leechao.didi.entity.Setmeal;
import com.leechao.didi.entity.SetmealDish;
import com.leechao.didi.mapper.SetmealMapper;
import com.leechao.didi.service.SetmealDishService;
import com.leechao.didi.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 新增套餐，同时保存套餐和菜品的关联关系表
     * @param setmealDto
     */
    @Transactional
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //1、保存套餐的基本信息，操作的是setmeal表，执行insert操作
        this.save(setmealDto);

        //获取setmealDish集合
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //2、保存套餐和菜品的关联信息，操作setmeal_dish表，执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐，同时删除套餐和菜品的关联数据
     * @param ids
     */
    @Transactional
    @Override
    public void removeWithDish(List<Long> ids) {
        //1、查询套餐状态，确定是否可以删除
        LambdaQueryWrapper<Setmeal>queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);//第一个条件查出前端传过来的id对应的setmeal
        queryWrapper.eq(Setmeal::getStatus,1);//第二个条件，查出status是1的setmeal，1为售卖中，不可删除

        int count = this.count(queryWrapper);//count()为框架的方法

        //2、如果不能删除，则抛出业务异常
        if(count>0){
            //不可删除的大于0，则不能删除
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        //3、如果可以删除，先删除套餐中的数据---setmeal
        this.removeByIds(ids);

        //4、再删除关系表中的数据---setmeal_dish
        //SQL：delete from setmeal_dish where setmeal_id in (ids);
        LambdaQueryWrapper<SetmealDish>queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getSetmealId,ids);

        setmealDishService.remove(queryWrapper1);
    }
}

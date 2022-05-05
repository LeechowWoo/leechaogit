package com.leechao.didi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leechao.didi.dto.DishDto;
import com.leechao.didi.entity.Dish;

public interface DishService extends IService<Dish> {
    //新增菜品，需要同时插入两张表，Dish表和Dishflavor
    public void saveWithFalvor(DishDto dishDto);

    //根据id查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    //修改菜品表以及对应的口味表
    public void updateWithFalvor(DishDto dishDto);
}

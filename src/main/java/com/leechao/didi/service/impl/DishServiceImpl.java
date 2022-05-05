package com.leechao.didi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leechao.didi.dto.DishDto;
import com.leechao.didi.entity.Dish;
import com.leechao.didi.entity.DishFlavor;
import com.leechao.didi.mapper.DishMapper;
import com.leechao.didi.service.DishFlavorService;
import com.leechao.didi.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * 新增菜品同时保存对应的口味数据
     * @param dishDto
     */
    @Transactional
    @Override
    public void saveWithFalvor(DishDto dishDto) {
        //1、先保存菜品的基本信息到Dish表中
        this.save(dishDto);//dishDto直接继承了Dish，所以可以直接将dishDto传入
        Long dishId = dishDto.getId();//对应菜品的id

        //菜品口味
        List<DishFlavor> flavors=dishDto.getFlavors();
        /*
        采用stream流的方式
         */
        flavors=flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //2、保存菜品口味表到dishflavor中
        dishFlavorService.saveBatch(flavors);//因为是将集合保存进去，所以需要用到saveBatch方法
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //1、查询菜品j基本信息，从dish表中查询
        Dish dish = this.getById(id);

        //对象拷贝，将dish中和DisgDto一致的属性拷贝上去
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //2、查询当前菜品对应的口味信息， 从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor>queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());

        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        //将dish中没有的dishDto属性单独拷贝
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 根据菜品id来修改菜品表以及对应的口味表
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFalvor(DishDto dishDto) {
        //1、先更新dish表
        this.updateById(dishDto);

        //2、清理当前菜品对应的口味表--dish_flavor表中的delete操作
        LambdaQueryWrapper<DishFlavor>queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //3、添加当前提交过来的口味数据--dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        //stream流？？？
        flavors=flavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }
}

package com.leechao.didi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leechao.didi.common.R;
import com.leechao.didi.dto.DishDto;
import com.leechao.didi.entity.Category;
import com.leechao.didi.entity.Dish;
import com.leechao.didi.entity.DishFlavor;
import com.leechao.didi.service.CategoryService;
import com.leechao.didi.service.DishFlavorService;
import com.leechao.didi.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String>save(@RequestBody DishDto dishDto){//将前端发送过来的数据封装到dishDto中
        log.info(dishDto.toString());
        dishService.saveWithFalvor(dishDto);

        //精确清理某个分类下面的菜品缓存数据
        String key="dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);

        return R.success("新增菜品成功！");
    }

    /**
     * 菜品信息分页controller
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page>page(int page,int pageSize,String name){
        //构造分页构造器对象
        Page<Dish>pageInfo=new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();

        //添加过滤条件，模糊查询
        queryWrapper.like(name!=null,Dish::getName,name);

        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);//根据更新时间降序排序

        //执行分页查询
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝，此处暂不深究？？？？？？
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);


            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    /**
     * 根据菜品id查询菜品信息和对应的口味信息controller
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){//@PathVariable用于接受来自前端的get请求中在url中携带的参数
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品controller
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String>update(@RequestBody DishDto dishDto){//将前端发送过来的数据封装到dishDto中
        log.info(dishDto.toString());
        dishService.updateWithFalvor(dishDto);

        //清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("dish_*");//使用通配符获得所有的以dish_开头的key
        //redisTemplate.delete(keys);

        //精确清理某个分类下面的菜品缓存数据
        String key="dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);

        return R.success("修改菜品成功！");
    }

    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */

    /*
    @GetMapping("/list")
    public R<List<Dish>>list(Dish dish){//为了增加通用性，这个不用Long作为接受参数，而采用了Dish实体
        //构建查询对象
        LambdaQueryWrapper<Dish>queryWrapper=new LambdaQueryWrapper<>();
        //添加查询条件
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());

        //再添加一个查询条件
        queryWrapper.eq(Dish::getStatus,1);//查询状态是1的数据，因为在数据库中状态为1代表的是在售，状态是0代表的是停售

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);
        return R.success(list);
    }
     */

    /**
     * 为实现是移动端查询菜品分类下对应菜品及菜品的口味信息，
     * 在后台系统查询菜品的基础上增加功能，不影响原后台查询功能的实现
     */
    @GetMapping("/list")
    public R<List<DishDto>>list(Dish dish){//为了增加通用性，这个不用Long作为接受参数，而采用了Dish实体
        List<DishDto> dishDtoList =null;
        //构建一个存在redis中的key
        String key="dish_"+dish.getCategoryId()+"_"+dish.getStatus();
        //先从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if(dishDtoList!=null){
            //如果存在，则直接返回，无需查询数据库
            return R.success(dishDtoList);
        }

        //构建查询对象
        LambdaQueryWrapper<Dish>queryWrapper=new LambdaQueryWrapper<>();
        //添加查询条件
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());

        //再添加一个查询条件
        queryWrapper.eq(Dish::getStatus,1);//查询状态是1的数据，因为在数据库中状态为1代表的是在售，状态是0代表的是停售

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);
        dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            Long dishId = item.getId();//获取当前菜品的id，然后根据当前菜品的id在口味表中查找对应的口味
            LambdaQueryWrapper<DishFlavor>lambdaQueryWrapper=new LambdaQueryWrapper<>();
            //SQL:select * from dish_flavor where id=?
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);//根据dishid查询对应的口味
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);//查出了对应的口味集合
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        //如果不存在，则需要查询数据库，然后将数据库中查询到的数据缓存到redis中
        redisTemplate.opsForValue().set(key,dishDtoList, 60,TimeUnit.MINUTES);//设置过期时间60分钟
        return R.success(dishDtoList);
    }
}
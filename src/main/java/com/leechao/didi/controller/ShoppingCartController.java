package com.leechao.didi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leechao.didi.common.BaseContext;
import com.leechao.didi.common.R;
import com.leechao.didi.dto.DishDto;
import com.leechao.didi.entity.ShoppingCart;
import com.leechao.didi.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车
 */
@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 购物车中添加菜品或套餐controller
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车信息：{}",shoppingCart);

        //设定用户id，指定是哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();//通过ThradLocal获取当前用户的id
        shoppingCart.setUserId(currentId);

        //查询当前菜品或套餐是否在购物车中，如果在就给number加1
        Long dishId = shoppingCart.getDishId();//如果前端传过来的是菜品，则当前对象的DishId不为空，否则就是套餐
        LambdaQueryWrapper<ShoppingCart>queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getDishId,dishId);
        if(dishId!=null){
            //当前添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else{
            //当前添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        //查询当前菜品或套餐是否在购物车中
        //上面框架完成的SQL语句为：select * from shopping_cart where user_id=? and dish_id/setmeal_id=?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        if(cartServiceOne!=null){
            //购物车中已存在，就在原来的数量上加一
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number+1);
            //调用service更新表
            shoppingCartService.updateById(cartServiceOne);
        }else{
            //如果不存在，则添加到购物车，数量默认为1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne=shoppingCart;
        }
        return R.success(cartServiceOne);
    }

    /**
     * 查看购物车controller
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>>list(){
        log.info("查看购物车...");

        LambdaQueryWrapper<ShoppingCart>queryWrapper=new LambdaQueryWrapper<>();
        Long currentId = BaseContext.getCurrentId();//获取当前登录用户的id
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 清空购物车controller
     * @return
     */
    @DeleteMapping("/clean")
    public R<String>clean(){
        //发送的SQL：delete from shopping_cart where user_id=?
        LambdaQueryWrapper<ShoppingCart>queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }
}
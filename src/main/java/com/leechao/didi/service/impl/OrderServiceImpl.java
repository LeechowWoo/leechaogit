package com.leechao.didi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leechao.didi.common.BaseContext;
import com.leechao.didi.common.CustomException;
import com.leechao.didi.entity.*;
import com.leechao.didi.mapper.OrderMapper;
import com.leechao.didi.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.AutomapConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders>implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;
    /**
     * 用户下单业务
     * @param orders
     */
    @Transactional//需要操作数据库，加入事务控制
    @Override
    public void submit(Orders orders) {
        //1、获取当前用户的用户id
        Long userId = BaseContext.getCurrentId();

        //2、根据当前用户的用户id查询购物车中的数据
        LambdaQueryWrapper<ShoppingCart>queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);
        if(shoppingCarts==null || shoppingCarts.size()==0){
            throw new CustomException("购物车为空，不能下单");//抛出业务异常
        }
        //查询用户信息
        User user = userService.getById(userId);
        //查询当前用户的地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if(addressBook==null){
            throw new CustomException("用户地址信息有误，不能下单");
        }

        //将orders表中的所有数据全部设定完成
        long orderId = IdWorker.getId();//IdWorker用来产生自增，不重复id

        AtomicInteger amount=new AtomicInteger(0);//原子操作，可以保证在多线程的情况下数据的安全性，此处用来保存金额

        /**
         * 遍历当前用户的购物车
         */
        List<OrderDetail>orderDetails=shoppingCarts.stream().map((item)->{
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额，通过前面遍历购物车得到最终金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        //3、向订单表中插入一条数据
        this.save(orders);
        //4、向订单明细表中插入数据，多条数据
        orderDetailService.saveBatch(orderDetails);

        //5、清空当前用户的购物车数据
        shoppingCartService.remove(queryWrapper);//这个查询条件是前面根据用户id查到的购物车信息
    }
}
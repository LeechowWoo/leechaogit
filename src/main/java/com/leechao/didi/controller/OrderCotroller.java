package com.leechao.didi.controller;

import com.leechao.didi.common.R;
import com.leechao.didi.entity.Orders;
import com.leechao.didi.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/order")
@RestController
@Slf4j
public class OrderCotroller {
    @Autowired
    private OrderService orderService;

    /**
     * 用户下单controller
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    /*
    此处只需将用户id传到后端即可，不需要将当前页面中现实的购物车数据传到后端
    因为根据当前用户的id可以在数据库中查到对应的购物车信息，不需要再与前端交互
     */
    public R<String> submit(@RequestBody  Orders orders){
        log.info("订单数据为：{}",orders);
        orderService.submit(orders);
        return R.success("下单成功！");
    }
}
package com.leechao.didi.dto;

import com.leechao.didi.entity.Dish;
import com.leechao.didi.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {
    /**
     * 在继承Dish的前提下又扩展了其他属性
     */
    //菜品对应的口味数据
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}

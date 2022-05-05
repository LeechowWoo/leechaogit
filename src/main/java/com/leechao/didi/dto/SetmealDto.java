package com.leechao.didi.dto;


import com.leechao.didi.entity.Setmeal;
import com.leechao.didi.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}

package com.leechao.didi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leechao.didi.entity.SetmealDish;
import com.leechao.didi.mapper.SetmealDishMapper;
import com.leechao.didi.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SetmealDishServiceImpl extends ServiceImpl<SetmealDishMapper, SetmealDish>implements SetmealDishService {
}

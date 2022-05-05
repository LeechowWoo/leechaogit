package com.leechao.didi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leechao.didi.entity.DishFlavor;
import com.leechao.didi.mapper.DishFlavorMapper;
import com.leechao.didi.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}

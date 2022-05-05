package com.leechao.didi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leechao.didi.entity.Category;

public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}

package com.leechao.didi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leechao.didi.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
}

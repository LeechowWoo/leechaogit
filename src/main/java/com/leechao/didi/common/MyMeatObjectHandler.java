package com.leechao.didi.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自定义的元数据对象处理器
 *      MetaObjectHandler接口是mybatisPlus为我们提供的的一个扩展接口，
 *      我们可以利用这个接口在我们插入或者更新数据的时候，为一些字段指定默认值。
 *      实现这个需求的方法不止一种，在sql层面也可以做到，在建表的时候也可以指定默认值。
 */
@Component
@Slf4j
public class MyMeatObjectHandler implements MetaObjectHandler {
    /**
     * 插入操作，自动填充
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {//执行insert语句时执行这个方法
        log.info("公共字段自动填充[insert]...");
        log.info(metaObject.toString());
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("createUser", BaseContext.getCurrentId());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }

    @Override
    public void updateFill(MetaObject metaObject) {//执行更新语句时执行这个方法
        log.info("公共字段自动填充[update]...");
        log.info(metaObject.toString());

        Long id=Thread.currentThread().getId();
        log.info("线程id为：{}",id);

        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }
}
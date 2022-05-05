package com.leechao.didi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leechao.didi.entity.User;
import com.leechao.didi.mapper.UserMapper;
import com.leechao.didi.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>implements UserService {
}

package com.leechao.didi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.leechao.didi.entity.AddressBook;
import com.leechao.didi.mapper.AddressBookMapper;
import com.leechao.didi.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

}

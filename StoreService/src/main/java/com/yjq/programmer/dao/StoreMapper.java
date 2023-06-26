package com.yjq.programmer.dao;

import com.yjq.programmer.domain.Store;
import com.yjq.programmer.domain.StoreExample;

import java.util.List;

public interface StoreMapper {

    List<Store> selectByExample(StoreExample example);
    int deleteByPrimaryKey(String id);
    Store selectByPrimaryKey(String id);

}

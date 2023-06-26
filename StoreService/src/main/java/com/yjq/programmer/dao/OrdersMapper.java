package com.yjq.programmer.dao;

import com.yjq.programmer.domain.Orders;
import com.yjq.programmer.domain.OrdersExample;

import java.util.List;

public interface OrdersMapper {
    int countByExample(OrdersExample example);


    int deleteByPrimaryKey(String id);

    int insertSelective(Orders record);

    List<Orders> selectByExample(OrdersExample example);

    Orders selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Orders record);

}

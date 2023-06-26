package com.yjq.programmer.dao;

import com.yjq.programmer.domain.OrderItem;
import com.yjq.programmer.domain.OrderItemExample;

import java.util.List;

public interface OrderItemMapper {

    int deleteByExample(OrderItemExample example);


    int insertSelective(OrderItem record);

    List<OrderItem> selectByExample(OrderItemExample example);


    int updateByPrimaryKeySelective(OrderItem record);

}

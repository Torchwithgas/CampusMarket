package com.yjq.programmer.service;

import com.yjq.programmer.dto.*;

import java.util.List;

public interface IStoreService {


    // 根据登录用户获取店铺信息
    ResponseDTO<StoreDTO> getByLoginUser(StoreDTO storeDTO);

    // 根据id获取店铺信息
    ResponseDTO<StoreDTO> getById(StoreDTO storeDTO);

    // 根据状态获取店铺信息
    ResponseDTO<List<StoreDTO>> getByState(StoreDTO storeDTO);

    // 结算页面展示订单信息
    ResponseDTO<List<CartDTO>> generateOrder(OrderDTO orderDTO);

    // 使用沙箱支付宝支付订单
    ResponseDTO<String> aliPayOrder(OrderDTO orderDTO);

    // 修改订单状态
    ResponseDTO<Boolean> updateOrderState(OrderDTO orderDTO);

    // 沙箱支付宝成功支付回调接口
    ResponseDTO<Boolean> aliPayOrderSuccess(OrderDTO orderDTO);

    // 获取所有订单总数
    ResponseDTO<Integer> getAllOrderTotal();

    // 根据用户获取订单数据
    ResponseDTO<List<OrderDTO>> getOrderByUser(OrderDTO orderDTO);

    // 修改某个订单详情的状态
    ResponseDTO<Boolean> updateOrderItemState(OrderItemDTO orderItemDTO);


    // 分页获取订单数据
    ResponseDTO<PageDTO<OrderDTO>> getOrderListByPage(PageDTO<OrderDTO> pageDTO);

    // 删除订单数据
    ResponseDTO<Boolean> removeOrder(OrderDTO orderDTO);

    // 根据用户id删除店铺数据
    ResponseDTO<Boolean> removeStoreByUserId(StoreDTO storeDTO);
    // 删除店铺数据
    ResponseDTO<Boolean> removeStore(StoreDTO storeDTO);

}

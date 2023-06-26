package com.yjq.programmer.service.impl;

import com.alipay.api.AlipayApiException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yjq.programmer.bean.CodeMsg;
import com.yjq.programmer.dao.OrderItemMapper;
import com.yjq.programmer.dao.OrdersMapper;
import com.yjq.programmer.dao.StoreMapper;
import com.yjq.programmer.domain.*;
import com.yjq.programmer.dto.*;
import com.yjq.programmer.enums.OrderStateEnum;
import com.yjq.programmer.enums.ProductStateEnum;
import com.yjq.programmer.feignclient.ProductClient;
import com.yjq.programmer.feignclient.UserClient;
import com.yjq.programmer.service.IStoreService;
import com.yjq.programmer.util.AliPayUtil;
import com.yjq.programmer.utils.CommonUtil;
import com.yjq.programmer.utils.CopyUtil;
import com.yjq.programmer.utils.SnowFlake;
import com.yjq.programmer.utils.UuidUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
@Transactional
public class StoreServiceImpl implements IStoreService {

    @Resource
    private StoreMapper storeMapper;

    @Resource
    private UserClient userClient;

    @Resource
    private OrderItemMapper orderItemMapper;

    @Resource
    private ProductClient productClient;

    @Resource
    private OrdersMapper ordersMapper;

    @Resource
    private AliPayUtil aliPayUtil;


    /**
     * 根据用户获取订单数据
     * @param orderDTO
     * @return
     */
    @Override
    public ResponseDTO<List<OrderDTO>> getOrderByUser(OrderDTO orderDTO) {
        if(CommonUtil.isEmpty(orderDTO.getUserId())) {
            return ResponseDTO.errorByMsg(CodeMsg.DATA_ERROR);
        }
        OrdersExample ordersExample = new OrdersExample();
        ordersExample.createCriteria().andUserIdEqualTo(orderDTO.getUserId());
        ordersExample.setOrderByClause("create_time desc");
        List<Orders> ordersList = ordersMapper.selectByExample(ordersExample);
        List<OrderDTO> orderDTOList = CopyUtil.copyList(ordersList, OrderDTO.class);
        // 封装订单详情数据
        for(OrderDTO item : orderDTOList) {
            OrderItemExample orderItemExample = new OrderItemExample();
            orderItemExample.createCriteria().andOrderIdEqualTo(item.getId());
            List<OrderItem> orderItemList = orderItemMapper.selectByExample(orderItemExample);
            List<OrderItemDTO> orderItemDTOList = CopyUtil.copyList(orderItemList, OrderItemDTO.class);
            for(OrderItemDTO orderItemDTO : orderItemDTOList) {
                Store store = storeMapper.selectByPrimaryKey(orderItemDTO.getStoreId());
                StoreDTO storeDTO = CopyUtil.copy(store, StoreDTO.class);
                if(storeDTO == null) {
                    orderItemDTO.setStoreDTO(new StoreDTO());
                } else {
                    orderItemDTO.setStoreDTO(storeDTO);
                }
            }
            item.setOrderItemDTOList(orderItemDTOList);
            UserDTO userDTO = new UserDTO();
            userDTO.setId(item.getUserId());
            ResponseDTO<UserDTO> responseUserDTO = userClient.getById(userDTO);
            if(CodeMsg.SUCCESS.getCode().equals(responseUserDTO.getCode())) {
                item.setUserDTO(responseUserDTO.getData());
            } else {
                item.setUserDTO(userDTO);
            }
        }
        return ResponseDTO.success(orderDTOList);
    }


    /**
     * 修改某个订单详情的状态
     * @param orderItemDTO
     * @return
     */
    @Override
    public ResponseDTO<Boolean> updateOrderItemState(OrderItemDTO orderItemDTO) {
        if(CommonUtil.isEmpty(orderItemDTO.getId()) || orderItemDTO.getState() == null) {
            return ResponseDTO.errorByMsg(CodeMsg.DATA_ERROR);
        }
        // 如果是取消，回退库存
        if(OrderStateEnum.CANCEL.getCode().equals(orderItemDTO.getState())) {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(orderItemDTO.getProductId());
            ResponseDTO<ProductDTO> responseDTO = productClient.getById(productDTO);
            if(!CodeMsg.SUCCESS.getCode().equals(responseDTO.getCode())) {
                return ResponseDTO.errorByMsg(CodeMsg.PRODUCT_NOT_EXIST);
            }
            productDTO = responseDTO.getData();
            productDTO.setStock(productDTO.getStock() + orderItemDTO.getQuantity());
            productClient.saveProduct(productDTO);
        }
        OrderItem orderItem = CopyUtil.copy(orderItemDTO, OrderItem.class);
        if(orderItemMapper.updateByPrimaryKeySelective(orderItem) == 0) {
            return ResponseDTO.errorByMsg(CodeMsg.ORDER_STATE_EDIT_ERROR);
        }
        return ResponseDTO.success(true);
    }


    /**
     * 分页获取订单数据
     * @param pageDTO
     * @return
     */
    @Override
    public ResponseDTO<PageDTO<OrderDTO>> getOrderListByPage(PageDTO<OrderDTO> pageDTO) {
        OrdersExample orderExample = new OrdersExample();
        // 判断是否进行关键字搜索
        if(!CommonUtil.isEmpty(pageDTO.getSearchContent())){
            orderExample.createCriteria().andNoLike("%"+pageDTO.getSearchContent()+"%");
        }
        orderExample.setOrderByClause("create_time desc");
        // 不知道当前页多少，默认为第一页
        if(pageDTO.getPage() == null){
            pageDTO.setPage(1);
        }
        pageDTO.setSize(5);
        PageHelper.startPage(pageDTO.getPage(), pageDTO.getSize());
        // 分页查出订单数据
        List<Orders> orderList = ordersMapper.selectByExample(orderExample);
        PageInfo<Orders> pageInfo = new PageInfo<>(orderList);
        // 获取数据的总数
        pageDTO.setTotal(pageInfo.getTotal());
        // 讲domain类型数据  转成 DTO类型数据
        List<OrderDTO> orderDTOList = CopyUtil.copyList(orderList, OrderDTO.class);
        for(OrderDTO orderDTO : orderDTOList){
            UserDTO userDTO = new UserDTO();
            userDTO.setId(orderDTO.getUserId());
            ResponseDTO<UserDTO> responseDTO = userClient.getById(userDTO);
            if(!responseDTO.getCode().equals(CodeMsg.SUCCESS.getCode())) {
                orderDTO.setUserDTO(userDTO);
            } else {
                orderDTO.setUserDTO(responseDTO.getData());
            }
            // 获取订单详情数据
            OrderItemExample orderItemExample = new OrderItemExample();
            orderItemExample.createCriteria().andOrderIdEqualTo(orderDTO.getId());
            List<OrderItem> orderItemList = orderItemMapper.selectByExample(orderItemExample);
            List<OrderItemDTO> orderItemDTOList = CopyUtil.copyList(orderItemList, OrderItemDTO.class);
            for(OrderItemDTO orderItemDTO : orderItemDTOList) {
                Store store = storeMapper.selectByPrimaryKey(orderItemDTO.getStoreId());
                if(store == null) {
                    orderItemDTO.setStoreDTO(CopyUtil.copy(new Store(),StoreDTO.class));
                } else {
                    orderItemDTO.setStoreDTO(CopyUtil.copy(store, StoreDTO.class));
                }
            }
            orderDTO.setOrderItemDTOList(orderItemDTOList);
        }
        pageDTO.setList(orderDTOList);
        return ResponseDTO.success(pageDTO);
    }

    /**
     * 删除订单数据
     * @param orderDTO
     * @return
     */
    @Override
    public ResponseDTO<Boolean> removeOrder(OrderDTO orderDTO) {
        if(CommonUtil.isEmpty(orderDTO.getId())){
            return ResponseDTO.errorByMsg(CodeMsg.DATA_ERROR);
        }
        // 删除订单数据
        if(ordersMapper.deleteByPrimaryKey(orderDTO.getId()) == 0){
            return ResponseDTO.errorByMsg(CodeMsg.ORDER_DELETE_ERROR);
        }
        // 删除订单详情数据
        OrderItemExample orderItemExample = new OrderItemExample();
        orderItemExample.createCriteria().andOrderIdEqualTo(orderDTO.getId());
        orderItemMapper.deleteByExample(orderItemExample);
        return ResponseDTO.successByMsg(true, "删除成功");
    }


    /**
     * 根据登录用户获取店铺信息
     * @param storeDTO
     * @return
     */
    @Override
    public ResponseDTO<StoreDTO> getByLoginUser(StoreDTO storeDTO) {
        // 获取当前登录用户信息
        UserDTO userDTO = new UserDTO();
        userDTO.setToken(storeDTO.getToken());
        ResponseDTO<UserDTO> responseUserDTO = userClient.getLoginUser(userDTO);
        if(!responseUserDTO.getCode().equals(CodeMsg.SUCCESS.getCode())) {
            return ResponseDTO.errorByMsg(CodeMsg.USER_SESSION_EXPIRED);
        }
        StoreExample storeExample = new StoreExample();
        storeExample.createCriteria().andUserIdEqualTo(responseUserDTO.getData().getId());
        List<Store> storeList = storeMapper.selectByExample(storeExample);
        if(storeList == null || storeList.size() == 0) {
            return ResponseDTO.success(new StoreDTO());
        }
        List<StoreDTO> storeDTOList = CopyUtil.copyList(storeList, StoreDTO.class);
        ProductDTO productDTO = new ProductDTO();
        productDTO.setStoreId(storeDTOList.get(0).getId());
        // 调用商品服务  获取店铺下的商品信息
        ResponseDTO<List<ProductDTO>> responseProductDTO = productClient.getByStore(productDTO);
//        ResponseDTO<List<ProductDTO>> responseProductDTO = JSONObject.parseObject(responseProduct, new TypeReference<ResponseDTO<List<ProductDTO>>>(){});
        if(!responseProductDTO.getCode().equals(CodeMsg.SUCCESS.getCode())) {
            storeDTOList.get(0).setProductDTOList(new ArrayList<>());
        }
        storeDTOList.get(0).setProductDTOList(responseProductDTO.getData());
        return ResponseDTO.success(storeDTOList.get(0));
    }

    /**
     * 根据id获取店铺信息
     * @param storeDTO
     * @return
     */
    @Override
    public ResponseDTO<StoreDTO> getById(StoreDTO storeDTO) {
        if(CommonUtil.isEmpty(storeDTO.getId())) {
            return ResponseDTO.errorByMsg(CodeMsg.DATA_ERROR);
        }
        Store store = storeMapper.selectByPrimaryKey(storeDTO.getId());
        StoreDTO responseStoreDTO = CopyUtil.copy(store, StoreDTO.class);
        ProductDTO productDTO = new ProductDTO();
        if(storeDTO.getSearchProductDTO() != null) {
            productDTO.setName(storeDTO.getSearchProductDTO().getName());
            productDTO.setState(storeDTO.getSearchProductDTO().getState());
        }
        productDTO.setStoreId(responseStoreDTO.getId());
        // 调用商品服务  获取店铺下的商品信息
        ResponseDTO<List<ProductDTO>> responseDTO = productClient.getByStore(productDTO);
        if(!responseDTO.getCode().equals(CodeMsg.SUCCESS.getCode())) {
            return ResponseDTO.success(new StoreDTO());
        }
        responseStoreDTO.setProductDTOList(responseDTO.getData());
        return ResponseDTO.success(responseStoreDTO);
    }

    /**
     * 根据状态获取店铺信息
     * @param storeDTO
     * @return
     */
    @Override
    public ResponseDTO<List<StoreDTO>> getByState(StoreDTO storeDTO) {
        if(storeDTO.getState() == null) {
            return ResponseDTO.errorByMsg(CodeMsg.DATA_ERROR);
        }
        StoreExample storeExample = new StoreExample();
        storeExample.createCriteria().andStateEqualTo(storeDTO.getState());
        List<Store> storeList = storeMapper.selectByExample(storeExample);
        return ResponseDTO.success(CopyUtil.copyList(storeList, StoreDTO.class));
    }

    /**
     * 结算页面订单展示
     * @param orderDTO
     * @return
     */
    @Override
    public ResponseDTO<List<CartDTO>> generateOrder(OrderDTO orderDTO) {
        if(orderDTO.getCartIdList() == null || orderDTO.getCartIdList().size() == 0) {
            return ResponseDTO.errorByMsg(CodeMsg.DATA_ERROR);
        }
        List<CartDTO> cartDTOList = new ArrayList<>();
        for(String cartId : orderDTO.getCartIdList()) {
            CartDTO cartDTO = new CartDTO();
            cartDTO.setId(cartId);
            ResponseDTO<CartDTO> responseDTO = userClient.getCartById(cartDTO);
            if(!responseDTO.getCode().equals(CodeMsg.SUCCESS.getCode())) {
                continue;
            }
            cartDTOList.add(responseDTO.getData());
        }
        return ResponseDTO.success(cartDTOList);
    }

    /**
     * 根据用户id删除店铺数据
     * @param storeDTO
     * @return
     */
    @Override
    public ResponseDTO<Boolean> removeStoreByUserId(StoreDTO storeDTO) {
        StoreExample storeExample = new StoreExample();
        storeExample.createCriteria().andUserIdEqualTo(storeDTO.getUserId());
        List<Store> storeList = storeMapper.selectByExample(storeExample);
        for(Store store : storeList) {
            removeStore(CopyUtil.copy(store, StoreDTO.class));
        }
        return ResponseDTO.success(true);
    }
    /**
     * 删除店铺数据
     * @param storeDTO
     * @return
     */
    @Override
    public ResponseDTO<Boolean> removeStore(StoreDTO storeDTO) {
        if(CommonUtil.isEmpty(storeDTO.getId())) {
            return ResponseDTO.errorByMsg(CodeMsg.DATA_ERROR);
        }
        // 删除店铺数据
        if(storeMapper.deleteByPrimaryKey(storeDTO.getId()) == 0) {
            return ResponseDTO.errorByMsg(CodeMsg.STORE_DELETE_ERROR);
        }
        // 删除该店铺下所有商品数据
        ProductDTO productDTO = new ProductDTO();
        productDTO.setStoreId(storeDTO.getId());
        productClient.removeProductByStoreId(productDTO);

        return ResponseDTO.successByMsg(true, "删除店铺成功！");
    }

    /**
     * 使用沙箱支付宝支付订单
     * @param orderDTO
     * @return
     */
    @Override
    public ResponseDTO<String> aliPayOrder(OrderDTO orderDTO) {
        // 判断是否进行继续支付
        if(!CommonUtil.isEmpty(orderDTO.getId())) {
            Orders orders = ordersMapper.selectByPrimaryKey(orderDTO.getId());
            return initAliPay(orders.getNo(),  orders.getTotalPrice());
        }
        if(orderDTO.getCartIdList() == null || orderDTO.getCartIdList().size() == 0) {
            return ResponseDTO.errorByMsg(CodeMsg.DATA_ERROR);
        }
        if(CommonUtil.isEmpty(orderDTO.getUserId())) {
            return ResponseDTO.errorByMsg(CodeMsg.USER_SESSION_EXPIRED);
        }
        List<OrderItem> orderItemList = new ArrayList<>();
        BigDecimal totalPrice = new BigDecimal("0.00");
        String orderNo = String.valueOf(new SnowFlake(2, 3).nextId());
        String orderId = UuidUtil.getShortUuid();
        // 获取用户挑选的商品信息
        for(String cartId : orderDTO.getCartIdList()) {
            CartDTO cartDTO = new CartDTO();
            cartDTO.setId(cartId);
            ResponseDTO<CartDTO> responseDTO = userClient.getCartById(cartDTO);
            if(!responseDTO.getCode().equals(CodeMsg.SUCCESS.getCode())) {
                return ResponseDTO.errorByMsg(CodeMsg.ORDER_PRODUCT_ERROR);
            }
            cartDTO = responseDTO.getData();
            // 判断商品库存是否足够
            if(cartDTO.getQuantity() > cartDTO.getProductDTO().getStock()) {
                CodeMsg codeMsg = CodeMsg.PRODUCT_STOCK_ERROR;
                codeMsg.setMsg("商品(" + cartDTO.getProductDTO().getName() + ")库存不足！" );
                return ResponseDTO.errorByMsg(codeMsg);
            }
            ProductDTO productDTO = cartDTO.getProductDTO();
            productDTO.setStock(productDTO.getStock() - cartDTO.getQuantity());
            productClient.saveProduct(productDTO);
            // 判断商品状态
            if(!ProductStateEnum.SUCCESS.getCode().equals(cartDTO.getProductDTO().getState())) {
                CodeMsg codeMsg = CodeMsg.PRODUCT_STATE_ERROR;
                codeMsg.setMsg("商品(" + cartDTO.getProductDTO().getName() + ")状态异常！" );
                return ResponseDTO.errorByMsg(codeMsg);
            }
            // 封装订单详情数据
            OrderItem orderItem = new OrderItem();
            orderItem.setId(UuidUtil.getShortUuid());
            orderItem.setProductName(cartDTO.getProductDTO().getName());
            orderItem.setProductId(cartDTO.getProductDTO().getId());
            orderItem.setProductPhoto(cartDTO.getProductDTO().getPhoto());
            orderItem.setProductPrice(cartDTO.getProductDTO().getPrice());
            orderItem.setOrderId(orderId);
            orderItem.setState(2);
            orderItem.setStoreId(cartDTO.getProductDTO().getStoreId());
            orderItem.setQuantity(cartDTO.getQuantity());
            orderItem.setSumPrice(new BigDecimal(orderItem.getQuantity()).multiply(orderItem.getProductPrice()));
            orderItemList.add(orderItem);
            totalPrice = totalPrice.add(orderItem.getSumPrice());
            // 移除购物车数据
            userClient.removeCart(cartDTO);
        }
        // 订单详情数据入库
        for(OrderItem orderItem : orderItemList){
            if(orderItemMapper.insertSelective(orderItem) == 0) {
                throw new RuntimeException(CodeMsg.ORDER_ADD_ERROR.getMsg());
            }
        }
        // 封装订单数据  并入库
        Orders orders = CopyUtil.copy(orderDTO, Orders.class);
        orders.setId(orderId);
        orders.setCreateTime(new Date());
        orders.setNo(orderNo);
        orders.setTotalPrice(totalPrice);
//        orders.setState(OrderStateEnum.WAIT.getCode());
        orders.setState(2);
        if(ordersMapper.insertSelective(orders) == 0) {
            throw new RuntimeException(CodeMsg.ORDER_ADD_ERROR.getMsg());
        }

        return initAliPay(orderNo, totalPrice);
    }

    /**
     * 修改订单状态
     * @param orderDTO
     * @return
     */
    @Override
    public ResponseDTO<Boolean> updateOrderState(OrderDTO orderDTO) {
        if(CommonUtil.isEmpty(orderDTO.getId())) {
            return ResponseDTO.errorByMsg(CodeMsg.DATA_ERROR);
        }
        if(orderDTO.getState() == null) {
            return ResponseDTO.errorByMsg(CodeMsg.ORDER_STATE_EMPTY);
        }
        // 获取订单信息
        Orders orders = ordersMapper.selectByPrimaryKey(orderDTO.getId());
        Integer oldState = orders.getState();
        orders.setState(orderDTO.getState());
        if(ordersMapper.updateByPrimaryKeySelective(orders) == 0) {
            return ResponseDTO.errorByMsg(CodeMsg.ORDER_STATE_EDIT_ERROR);
        }
        orders.setState(oldState);
        // 获取订单详情数据
        OrderItemExample orderItemExample = new OrderItemExample();
        orderItemExample.createCriteria().andOrderIdEqualTo(orders.getId());
        List<OrderItem> orderItemList = orderItemMapper.selectByExample(orderItemExample);
        for(OrderItem orderItem : orderItemList) {
            // 如果是取消订单，且原先订单是已支付的订单，要回退库存
            // 如果是未支付订单 且原先订单是已支付订单 要回退库存
            if((OrderStateEnum.CANCEL.getCode().equals(orderDTO.getState()) && OrderStateEnum.PAY.getCode().equals(orders.getState()))
                || (OrderStateEnum.WAIT.getCode().equals(orderDTO.getState()) && OrderStateEnum.PAY.getCode().equals(orders.getState()))) {
                ProductDTO productDTO = new ProductDTO();
                productDTO.setId(orderItem.getProductId());
                ResponseDTO<ProductDTO> responseDTO = productClient.getById(productDTO);
                if(!CodeMsg.SUCCESS.getCode().equals(responseDTO.getCode())) {
                    continue;
                }
                productDTO = responseDTO.getData();
                productDTO.setStock(productDTO.getStock() + orderItem.getQuantity());
                productClient.saveProduct(productDTO);
            }
            // 如果是支付订单 且原先订单是未支付或取消订单
            if(OrderStateEnum.PAY.getCode().equals(orderDTO.getState()) && (OrderStateEnum.WAIT.getCode().equals(orders.getState())
                || OrderStateEnum.CANCEL.getCode().equals(orders.getState()))) {
                aliPayOrderSuccess(CopyUtil.copy(orders, OrderDTO.class));
            }
            orderItem.setState(orderDTO.getState());
            orderItemMapper.updateByPrimaryKeySelective(orderItem);
        }

        return ResponseDTO.success(true);
    }

    /**
     * 初始化支付信息
     * @param orderNo
     * @param totalPrice
     * @return
     */
    public ResponseDTO<String> initAliPay(String orderNo, BigDecimal totalPrice) {
        // 封装沙箱支付宝支付信息
        AliPayBean alipayBean = new AliPayBean();
        alipayBean.setOut_trade_no(orderNo);
        alipayBean.setSubject("支付宝支付");
        alipayBean.setTotal_amount(String.valueOf(totalPrice));
        alipayBean.setBody("欢迎下单！！");
        String pay = null;
        try {
            pay = aliPayUtil.pay(alipayBean);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //pay
        return ResponseDTO.success("支付成功!");
    }

    /**
     * 沙箱支付宝成功支付回调接口
     * @param orderDTO
     * @return
     */
    @Override
    public ResponseDTO<Boolean> aliPayOrderSuccess(OrderDTO orderDTO) {
        if(CommonUtil.isEmpty(orderDTO.getNo())) {
            return ResponseDTO.errorByMsg(CodeMsg.DATA_ERROR);
        }
        OrdersExample ordersExample = new OrdersExample();
        ordersExample.createCriteria().andNoEqualTo(orderDTO.getNo());
        List<Orders> orderList = ordersMapper.selectByExample(ordersExample);
        if(orderList == null || orderList.size() == 0) {
            return ResponseDTO.errorByMsg(CodeMsg.ORDER_NOT_EXIST);
        }
        Orders orders = orderList.get(0);
        if(!OrderStateEnum.WAIT.getCode().equals(orders.getState()) && orderDTO.getState() == null) {
            // 不是待支付的状态，不执行后续逻辑
            return ResponseDTO.errorByMsg(CodeMsg.ORDER_NOT_WAIT_PAY);
        }
        orders.setState(OrderStateEnum.PAY.getCode());
        ordersMapper.updateByPrimaryKeySelective(orders);
        // 减少商品对应的库存  并修改订单详情状态
        OrderItemExample orderItemExample = new OrderItemExample();
        orderItemExample.createCriteria().andOrderIdEqualTo(orders.getId());
        List<OrderItem> orderItemList = orderItemMapper.selectByExample(orderItemExample);
        for(OrderItem orderItem : orderItemList) {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(orderItem.getProductId());
            ResponseDTO<ProductDTO> responseDTO = productClient.getById(productDTO);
            if(!CodeMsg.SUCCESS.getCode().equals(responseDTO.getCode())) {
                continue;
            }
            productDTO = responseDTO.getData();
            productDTO.setStock(productDTO.getStock() - orderItem.getQuantity());
            productClient.saveProduct(productDTO);
            orderItem.setState(OrderStateEnum.PAY.getCode());
            orderItemMapper.updateByPrimaryKeySelective(orderItem);
        }
        return ResponseDTO.successByMsg(true, "支付成功！");
    }

    /**
     * 获取所有订单总数
     * @return
     */
    @Override
    public ResponseDTO<Integer> getAllOrderTotal() {
        int total = ordersMapper.countByExample(new OrdersExample());
        return ResponseDTO.success(total);
    }


}

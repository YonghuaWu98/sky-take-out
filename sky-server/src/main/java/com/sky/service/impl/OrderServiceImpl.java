package com.sky.service.impl;

import cn.hutool.db.sql.Order;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.BaseException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.service.UserService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.sky.constant.MessageConstant.ADDRESS_BOOK_IS_NULL;
import static com.sky.constant.MessageConstant.SHOPPING_CART_IS_NULL;
import static com.sky.entity.Orders.CANCELLED;

/**
 * @author 吴勇华
 * @description: TODO
 */

@Service
public class OrderServiceImpl implements OrderService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderDetailMapper orderDetailMapper;
    @Resource
    private AddressBookMapper addressBookMapper;
    @Resource
    private ShoppingCartMapper shoppingCartMapper;
    @Resource
    private WeChatPayUtil weChatPayUtil;
    /*
     * 用户下单
     * @return: OrderSubmitVO
     **/
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        // 处理异常（地址簿不能为空，购物车不能为空）
        // 获取地址簿信息
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new OrderBusinessException(ADDRESS_BOOK_IS_NULL);
        }
        // 获取购物车数据
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        if (shoppingCarts == null || shoppingCarts.size() < 1) {
            throw new ShoppingCartBusinessException(SHOPPING_CART_IS_NULL);
        }
        //向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setAddress(addressBook.getDetail());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(orders.PENDING_PAYMENT);
        orderMapper.insert(orders);
        //向订单详细表中插入 n 条数据
        List<OrderDetail> orderDetials = new ArrayList<>();
        for (ShoppingCart sc : shoppingCarts) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(sc, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetials.add(orderDetail);
        }
        orderDetailMapper.insertByBatch(orderDetials);
        // 清空购物车
        shoppingCartMapper.delete(shoppingCart);
        //返回 OrderSubmitVO
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "一家五口外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /*
     * 查询订单详情
     * @return: Orders
     **/
    public OrderVO getOrderDetailById(Long id) {
        // 查找订单数据
        Orders orders = orderMapper.getById(id);
        // 查找订单详情信息
        List<OrderDetail> orderDetailList = orderDetailMapper.getById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /*
     * 历史订单查询
     * @return: OrderVO
     **/
    public PageResult queryOrdersByPage(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 设置分页
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Long userId = BaseContext.getCurrentId();
        ordersPageQueryDTO.setUserId(userId);
        // 分页条件查询
        Page<Orders> page = orderMapper.queryByPage(ordersPageQueryDTO);
        List<OrderVO> list = new ArrayList<>();
        // 查询出订单明细，并封装入OrderVO进行响应
        if (page != null && page.getTotal() > 0) {
            for (Orders orders : page) {
                Long orderId = orders.getId();// 订单id

                // 查询订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.getById(orderId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);

                list.add(orderVO);
            }
        }

        return new PageResult(page.getTotal(), list);
    }

    /*
     * 取消订单
     * @return: void
     **/
    public void cancelOrder(Long id) throws Exception{
        // 根据 id 获取订单信息
        Orders order = orderMapper.getById(id);
        // 判断 order 是否为空，为空则抛出异常
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 订单状态 1：待付款 2：待接单 3：已接单 4：派送中 5：已完成 6：已取消
        if (order.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        if (order.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    order.getNumber(), //商户订单号
//                    order.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            order.setPayStatus(Orders.REFUND);
        }
        // 更新订单状态、取消原因、取消时间
        order.setId(id);
        order.setCancelTime(LocalDateTime.now());
        order.setStatus(CANCELLED);
        // 更新订单信息
        orderMapper.update(order);
    }

    /*
     * 再来一单
     * @return: Orders
     **/
    public void repetition(Long id) {
        // 查询订单详情数据
        List<OrderDetail> orderDetailList = orderDetailMapper.getById(id);
        List<ShoppingCart> shoppingCartList = new ArrayList<>();
        ShoppingCart shoppinCart = null;
        for (OrderDetail orderDetail : orderDetailList) {
            shoppinCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppinCart, "id");
            // 设置购物车创建时间
            shoppinCart.setCreateTime(LocalDateTime.now());
            // 设置用户 id
            shoppinCart.setUserId(BaseContext.getCurrentId());
            shoppingCartList.add(shoppinCart);
        }
        // 添加到购物车
        shoppingCartMapper.insertByBatch(shoppingCartList);
    }
}

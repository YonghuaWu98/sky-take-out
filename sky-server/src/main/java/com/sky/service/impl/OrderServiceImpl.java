package com.sky.service.impl;

import cn.hutool.db.sql.Order;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
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
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sky.constant.MessageConstant.ADDRESS_BOOK_IS_NULL;
import static com.sky.constant.MessageConstant.SHOPPING_CART_IS_NULL;
import static com.sky.entity.Orders.*;

/**
 * @author 吴勇华
 * @description: TODO
 */
@Slf4j
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
    @Resource
    private WebSocketServer webSocketServer;
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
        // 通过 websocket 向客户端浏览器推送消息 type orderId content
        Map map = new HashMap();
        map.put("type", 1);
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号 ： " + outTradeNo);
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
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
        List<OrderVO> OrderVOList = new ArrayList<>();
        List<Orders> ordersList = page.getResult();
        // 查询出订单明细，并封装入OrderVO进行响应
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                Long orderId = orders.getId();// 订单id

                // 查询订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.getById(orderId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);

                OrderVOList.add(orderVO);
            }
        }

        return new PageResult(page.getTotal(), OrderVOList);
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

    /*
     * 订单搜索
     * @return: PageResult
     **/
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 获取搜索的订单页
        int page = ordersPageQueryDTO.getPage();
        // 获取页大小
        int pageSize = ordersPageQueryDTO.getPageSize();
        // 开始分页
        PageHelper.startPage(page, pageSize);

        Page<Orders> pageOrders = orderMapper.queryByPage(ordersPageQueryDTO);
        // 当前查询到的数据为空
        if (pageOrders == null || pageOrders.getTotal() == 0) {
            return new PageResult();
        }
        // 不为空
        ArrayList<OrderVO> orderVOS = new ArrayList<>();
        for (Orders ods : pageOrders) {
            OrderVO orderVO = new OrderVO();
            Long orderId = ods.getId();// 订单id

            // 查询订单明细
            List<OrderDetail> orderDetails = orderDetailMapper.getById(orderId);
            // 开始属性拷贝

            BeanUtils.copyProperties(ods, orderVO);
            orderVO.setOrderDetailList(orderDetails);
            // 将订单菜品信息封装到orderVO中，并添加到orderVOList
            String orderDishes = getOrderDishesStr(ods);
            orderVO.setOrderDishes(orderDishes);
            orderVOS.add(orderVO);
        }
        // 返回数据
        return new PageResult(pageOrders.getTotal(), orderVOS);
    }
    /*
     * 根据订单 id 获取菜品信息字符串
     * @return: String
     **/

    private String getOrderDishesStr(Orders ods) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getById(ods.getId());
        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());
        // 将订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }


    /*
     * 统计各个状态下的订单数量
     * @return: OrderStatisticsVO
     **/
    public OrderStatisticsVO statistics() {
        // 获取待接单订单条数
        int toBeConfirmed = orderMapper.statistics(Orders.TO_BE_CONFIRMED);
        // 获取待派送订单条数
        int confirmed = orderMapper.statistics(Orders.CONFIRMED);
        // 获取派送中的订单条数
        int deliveryInProgress = orderMapper.statistics(DELIVERY_IN_PROGRESS);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        return orderStatisticsVO;
    }

    /*
     * 根据订单 id 查询订单详情
     * @return: OrderVO
     **/
    public OrderVO details(Long id) {
        // 获取订单数据
        Orders orders = orderMapper.getById(id);
        // 获取订单详情数据
        List<OrderDetail> detailList = orderDetailMapper.getById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(detailList);
        return orderVO;
    }



    /*
     * 接单
     * @return: void
     **/
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    /*
     * 拒单
     * @return: void
     **/
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {

        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        // 订单只有存在且状态为 2（待接单）才可以拒单
        if (orders == null && !orders.getStatus().equals(TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Integer payStatus = orders.getStatus();
        if (payStatus == Orders.PAID) {
            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    orders.getNumber(),
//                    orders.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
//            log.info("申请退款：{}", refund);
        }
        // 拒单需要退款，根据订单id更新订单状态、拒单原因、取消时间
        orders.setId(orders.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /*
     * 取消订单
     * @return: void
     **/
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());

        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus == 1) {
            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    ordersDB.getNumber(),
//                    ordersDB.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
            log.info("申请退款：{}", 0.1);
        }

        // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /*
     * 派送订单
     * @return: void
     **/
    public void delivery(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为3
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }

    /*
     * 完成订单
     * @return: void
     **/
    public void complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为 4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);
    }
}

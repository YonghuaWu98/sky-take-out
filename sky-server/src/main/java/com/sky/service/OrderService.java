package com.sky.service;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

import java.util.List;

public interface OrderService {
    /*
     * 用户下单
     * @return: OrderSubmitVO
     **/
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);
    /*
     * 查询订单详情
     * @param: id
     * @return: Orders
     **/
    OrderVO getOrderDetailById(Long id);

    /*
     * 历史订单查询
     * @return: OrderVO
     **/
    PageResult queryOrdersByPage(OrdersPageQueryDTO ordersPageQueryDTO);
    /*
     * 取消订单
     * @return: void
     **/
    void cancelOrder(Long id) throws Exception;

    /*
     * 再来一单
     * @return: void
     **/
    void repetition(Long id);
}

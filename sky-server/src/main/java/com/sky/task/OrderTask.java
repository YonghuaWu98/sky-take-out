package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 吴勇华
 * @description: TODO
 */

@Component
@Slf4j
public class OrderTask {

    @Resource
    private OrderMapper orderMapper;
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeOutOrders() {
        log.info("定时处理超时订单：{}", LocalDateTime.now());
        // 以当前时间为参考，获取前 15 分钟的时间戳
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        // 找出那些待支付订单中已经超时的订单
        List<Orders> timeOutOrders = orderMapper.getByStatusAndTimeLT(Orders.PENDING_PAYMENT, time);
        if (timeOutOrders == null && timeOutOrders.size() < 1) return;
        for (Orders orders : timeOutOrders) {
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelTime(LocalDateTime.now());
            orders.setCancelReason("订单超时，自动取消");
            orderMapper.update(orders);
        }

    }
    @Scheduled(cron = "0 0 1 * * ?")// 每天凌晨 1 点触发一次
    public void processDeliverStatus() {
        log.info("定时处理派送中的订单：{}", LocalDateTime.now());

        // 以当前时间为参考，获取前 60 分钟的时间戳
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        // 找出那些待支付订单中已经超时的订单
        List<Orders> deliveryOrders = orderMapper.getByDeliveryStatus(Orders.DELIVERY_IN_PROGRESS, time);
        if (deliveryOrders == null && deliveryOrders.size() < 1) return;
        for (Orders orders : deliveryOrders) {
            orders.setStatus(Orders.COMPLETED);
            orderMapper.update(orders);
        }
    }
}

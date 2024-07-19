package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import net.bytebuddy.asm.Advice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    /*
     * 用户下单，保存订单
     * @return: void
     **/
    void insert(Orders orders);
    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);
    /*
     * 通过订单号获取订单详情
     * @return: void
     **/
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /*
     * 分页查询历史订单数据
     * @return: Page<OrderVO>
     **/
    Page<Orders> queryByPage(OrdersPageQueryDTO ordersPageQueryDTO);

    /*
     * 统计各个状态的订单数量
     * @return: OrderStatisticsVO
     **/
    @Select("select count(*) from orders where status = #{status}")
    int statistics(Integer status);
    /*
     * 获取超时订单
     * @return: void
     **/
    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> getByStatusAndTimeLT(Integer status, LocalDateTime time);
    /*
     * 获取派送订单
     * @return: List<Orders>
     **/
    @Select("select * from orders where status = #{deliveryStatus} and order_time < #{time}")
    List<Orders> getByDeliveryStatus(Integer deliveryStatus, LocalDateTime time);
}

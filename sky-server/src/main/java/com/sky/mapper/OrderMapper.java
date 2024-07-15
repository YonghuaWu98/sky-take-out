package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper {

    /*
     * 用户下单，保存订单
     * @return: void
     **/
    void insert(Orders orders);
}

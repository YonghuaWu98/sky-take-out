package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ShoppingCartService {
    /*
     * 添加购物车
     * @return: void
     **/
    void add(ShoppingCartDTO shoppingCartDTO);
    /*
     * 查看购物车
     * @return: List<ShoppingCart>
     **/
    List<ShoppingCart> list();
    /*
     * 清空购物车
     * @return: void
     **/

    void clean();
    /*
     * 删除购物车中一个商品
     * @return: void
     **/
    void subProduct(ShoppingCartDTO shoppingCartDTO);
}

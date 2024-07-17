package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import com.sky.service.ShoppingCartService;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author 吴勇华
 * @description: TODO 购物车相关功能
 */
@Mapper
public interface ShoppingCartMapper {

    /*
     * 动态条件查询购物车商品
     * @return: void
     **/
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /*
     * 更新购物车数据
     * @return: void
     **/
//    @Update("update shopping_cart set number = #{number}")
    void update(ShoppingCart sc);
    /*
     * 添加购物车商品
     * @return: void
     **/
    void add(ShoppingCart shoppingCart);

    /*
     * 清空当前用户的购物车数据
     * @return: void
     **/
    void delete(ShoppingCart shoppingCart);
}

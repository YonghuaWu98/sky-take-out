package com.sky.service.impl;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 吴勇华
 * @description: TODO
 */
@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Resource
    private ShoppingCartMapper shoppingCartMapper;
    @Resource
    private SetmealMapper setmealMapper;

    @Resource
    private DishMapper dishMapper;
    /*
     * 添加购物车
     * @return: void
     **/
    public void add(ShoppingCartDTO shoppingCartDTO) {
        // 判断当前加入购物车的商品是否已经存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> sc = shoppingCartMapper.list(shoppingCart);
        if (sc != null && sc.size() > 0) {
            // 如果存在，将数量加一
            sc.get(0).setNumber(sc.get(0).getNumber() + 1);
            shoppingCartMapper.update(sc.get(0));
            return;
        }
        // 如果不存在，添加一条购物车商品数据
        // 判断当前添加到购物车的是菜品还是套餐
        if (shoppingCart.getDishId() == null) {
            Setmeal setmeal = setmealMapper.querySetmealById(shoppingCart.getSetmealId());
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setAmount(setmeal.getPrice());
            shoppingCart.setImage(setmeal.getImage());
        }else {
            Dish dish = dishMapper.getById(shoppingCart.getDishId());
            shoppingCart.setName(dish.getName());
            shoppingCart.setAmount(dish.getPrice());
            shoppingCart.setImage(dish.getImage());
        }
        // 公共字段
        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartMapper.add(shoppingCart);
    }

    /*
     * 查看购物车
     * @return: List<ShoppingCart>
     **/
    public List<ShoppingCart> list() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        return shoppingCarts;
    }

    /*
     * 清空购物车
     * @return: void
     **/
    public void clean() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        shoppingCartMapper.delete(shoppingCart);
    }

    /*
     * 删除购物车中一个商品
     * @return: void
     **/
    public void subProduct(ShoppingCartDTO shoppingCartDTO) {

        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        // 判断当前要删除的商品的 number 是否大于 1
        // 查询当前商品是否多次被加入购物车
        List<ShoppingCart> sc = shoppingCartMapper.list(shoppingCart);
        if (sc.get(0).getNumber() > 1) { //大于 1 表示当前商品多次添加购物车
            // number 减一，然后更新到数据库
            sc.get(0).setNumber(sc.get(0).getNumber() - 1);
            shoppingCartMapper.update(sc.get(0));
            return;
        }
        // 等于 1
        shoppingCartMapper.delete(shoppingCart);
    }
}

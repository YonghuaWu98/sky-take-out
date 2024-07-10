package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 吴勇华
 * @description: TODO 购物车模块
 */
@RestController
@RequestMapping("/user/shoppingCart")
@Api(tags = "C端-购物车相关功能")
@Slf4j
public class ShoppingCartController {

    @Resource
    private ShoppingCartService shoppingCartService;

    @ApiOperation(value = "添加购物车")
    @PostMapping("/add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("当前添加的商品信息为：{}", shoppingCartDTO);
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }
    @GetMapping("/list")
    @ApiOperation(value = "查看购物车")
    public Result<List<ShoppingCart>> queryShoppingCart() {

        List<ShoppingCart> shoppingCarts = shoppingCartService.list();
        return Result.success(shoppingCarts);
    }
    @DeleteMapping("/clean")
    @ApiOperation(value = "清空购物车")
    public Result clean() {
        shoppingCartService.clean();
        return Result.success();
    }
    @PostMapping("/sub")
    @ApiOperation(value = "删除购物车中一个商品")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {

        shoppingCartService.subProduct(shoppingCartDTO);
        return Result.success();
    }
}

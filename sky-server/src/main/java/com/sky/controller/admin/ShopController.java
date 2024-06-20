package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result setStatus(@PathVariable Integer status) {
        log.info("设置店铺的营业状态：{}", status == 1 ? "营业中" : "打烊中");
        stringRedisTemplate.opsForValue().set(MessageConstant.SHOP_STATUS_KEY, status.toString());
        return Result.success();
    }
    @GetMapping("/status")
    @ApiOperation("获取店铺状态")
    public Result<Integer> getStatus() {

        String status = stringRedisTemplate.opsForValue().get(MessageConstant.SHOP_STATUS_KEY);
        Integer sta = Integer.valueOf(status);
        log.info("获取店铺状态:{}", sta == 1 ? "营业中" : "打烊中");
        return Result.success(sta);
    }
}

package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.UserService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import static com.sky.constant.DishRedisKeyConstant.CACHE_DISH_KEY;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Api(tags = "C端-菜品相关接口")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @GetMapping("/list")
    @ApiOperation(value ="根据分类id查询菜品")

    public Result<List<DishVO>> listDishWithFlavorByCategoryId(@RequestParam Long categoryId) {
        log.info("当前查询的菜品分类id为：{}", categoryId);
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品
        List<DishVO> dishes = dishService.listWithFlavor(dish);
        return Result.success(dishes);
    }


}

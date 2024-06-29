package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.service.UserService;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Api(tags = "C端-套餐相关接口")
@Slf4j
public class SetmealController {
    @Resource
    private SetmealService setmealService;
    @GetMapping("/list")
    @ApiOperation(value ="根据分类id查询套餐信息")
    public Result<List<Setmeal>> listSetmeal(@RequestParam Long categoryId) {

        log.info("当前查询的套餐的分类id为{}", categoryId);
        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        //展示在售的套餐
        setmeal.setStatus(StatusConstant.ENABLE);
        List<Setmeal> setmeals = setmealService.list(setmeal);
        return Result.success(setmeals);
    }

    @GetMapping("/dish/{id}")
    @ApiOperation(value ="根据套餐id查询包含的菜品")
    public Result<List<DishItemVO>> listSetmealIncludeDishes(@PathVariable Long id) {

        log.info("当前查询套餐的id为：{}", id);
        List<DishItemVO> DishItemVO = setmealService.queryIncludeDishes(id);
        return Result.success(DishItemVO);
    }
}

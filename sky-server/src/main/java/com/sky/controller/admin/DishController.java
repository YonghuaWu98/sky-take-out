package com.sky.controller.admin;



import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/dish")
@Api(tags = "菜品管理相关接口")
public class DishController {

    @Resource
    private DishService dishService;

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> queryByPage(DishPageQueryDTO dishPageQueryDTO) {
        PageResult pageResult = dishService.queryByPage(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    @PostMapping()
    @ApiOperation("新增菜品")
    public Result saveDish(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    @DeleteMapping()
    @ApiOperation("批量删除菜品")
    public Result deleteDishes(@RequestParam List<Long> ids) {
        log.info("当前要删除的菜品id为{}",ids);

        if (ids != null && ids.size() > 0) {
            dishService.deleteBatch(ids);
        }
        return Result.success();
    }


    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> queryById(@PathVariable Long id) {
        log.info("当前要删除的菜品id为{}",id);
        DishVO dishVO = dishService.getDishWithFlavorById(id);
        return Result.success(dishVO);
    }
    @PutMapping()
    @ApiOperation("修改菜品")
    public Result updateDish(@RequestBody DishDTO dishDTO) {
        log.info("当前要修改的菜品信息{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售、停售")
    public Result setStatus(@PathVariable Integer status, Long id) {
        log.info("设置是否起售：{}", status);
        dishService.setStatus(status, id);
        return Result.success();
    }
}

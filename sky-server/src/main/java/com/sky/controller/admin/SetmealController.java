package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static com.sky.constant.DishRedisKeyConstant.CACHE_DISH_KEY;

@RestController("adminSetmealController")
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
@Slf4j
public class SetmealController {

    @Resource
    private SetmealService setmealService;

    @GetMapping("/page")
    @ApiOperation(value ="分页查询")
    public Result<PageResult> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("当前请求的页为：{}, 页大小为：{}", setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }


    @PostMapping()
    @ApiOperation(value ="新增套餐")
    @CacheEvict(cacheNames = CACHE_DISH_KEY, key = "#setmealDTO.cetagoryId")//新增套餐，与之关联的套餐分类的缓存需要删除
    public Result<String> addSetmeal(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐:{}", setmealDTO);
        setmealService.add(setmealDTO);
        return Result.success();
    }


    @GetMapping("/{id}")
    @ApiOperation(value ="根据id查询套餐")
    public Result<SetmealVO> querySetmealById(@PathVariable Long id) {
        log.info("当前查询的套餐id为:{}", id);
        SetmealVO setmealVO = setmealService.querySetmealById(id);
        return Result.success(setmealVO);
    }

    @PutMapping()
    @ApiOperation(value ="修改套餐")
    @CacheEvict(cacheNames = CACHE_DISH_KEY, allEntries = true)
    public Result<String> querySetmealById(@RequestBody SetmealDTO setmealDTO) {
        log.info("当前修改的套餐为:{}", setmealDTO);
        setmealService.updateSetmeal(setmealDTO);
        return Result.success();
    }


    @PostMapping("/status/{status}")
    @ApiOperation(value ="套餐起售、停售")
    @CacheEvict(cacheNames = CACHE_DISH_KEY, allEntries = true)
    public Result<String> setStatus(@PathVariable Integer status, @RequestParam Long id) {
        log.info("当前套餐设置状态为：{}", status == 0 ? "停售" : "起售");
        setmealService.setStatus(status, id);
        return Result.success();
    }
    @DeleteMapping()
    @ApiOperation(value ="批量删除套餐")
    @CacheEvict(cacheNames = CACHE_DISH_KEY, allEntries = true)
    public Result<String> deleteSetmealByIds(@RequestParam List<Long> ids) {
        setmealService.delete(ids);
        return Result.success();
    }
}



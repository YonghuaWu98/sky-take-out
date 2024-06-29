package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    /**
     * 分页查询套餐数据
     * @return: PageResult
     **/
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);


    /**
     * 新增套餐
     * @return: void
     **/
    void add(SetmealDTO setmealDTO);



    /**
     * 根据分类 id 查询套餐信息
     * @return: List<Setmeal>
     **/
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐 id 查询包含的菜品
     * @return: List<DishItemVO>
     **/
    List<DishItemVO> queryIncludeDishes(Long id);

    /**
     * 根据 id 查询套餐
     * @return: SetmealVO
     **/
    SetmealVO querySetmealById(Long id);


    /**
     * 修改套餐
     * @return: void
     **/
    void updateSetmeal(SetmealDTO setmealDTO);
    /**
     * 起售、停售套餐
     * @return: void
     **/

    void setStatus(Integer status, Long id);

    /**
     * 批量删除套餐
     * @return: void
     **/
    void delete(List<Long> ids);
}

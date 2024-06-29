package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 菜品分页查询
     * @return: void
     **/
    PageResult queryByPage(DishPageQueryDTO dishPageQueryDTO);
    /**
     * 新增菜品和口味
     * @return: void
     **/
    void saveWithFlavor(DishDTO dishDTO);
    /**
     * 批量删除菜品
     **/
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询菜品和口味数据
     * @return: Dish
     **/
    DishVO getDishWithFlavorById(Long id);
    /**
     * 修改菜品
     * @return: void
     **/
    void updateWithFlavor(DishDTO dishDTO);
    /**
     * 菜品起售、停售
     * @return: void
     **/
    void setStatus(Integer status, Long id);
    /**
     * 查询菜品和口味
     * @return: List<DishVO>
     **/
    List<DishVO> listWithFlavor(Dish dish);

    /**
     * 根据分类id查询菜品
     * @param: dishDTO
     * @return: List<Dish>
     **/
    List<Dish> listByCategoryId(DishDTO dishDTO);
}

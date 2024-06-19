package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
@Mapper
public interface SetmealDishMapper {

    /**
     * 根据id查询套餐与菜品关联的数量
     * @return: List<Long>
     * @param ids
    */
    List<Long> getSetmealIdsByDishIds(List<Long> ids);
}

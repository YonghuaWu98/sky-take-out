package com.sky.mapper;

import com.sky.entity.SetmealDish;
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

    /**
     * 根据套餐 id 查询套餐与菜品的关系
     * @return: List<SetmealDish>
     **/
    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> queryById(Long id);
    /**
     * 批量删除套餐和菜品关系
     * @return: void
     **/
    void deleteSetmealWithDish(List<Long> ids);
}

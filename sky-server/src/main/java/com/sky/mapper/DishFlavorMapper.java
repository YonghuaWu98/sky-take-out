package com.sky.mapper;

import com.sky.annotations.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    /**
     * 批量插入口味数据
     **/
    @AutoFill(value = OperationType.INSERT)
    void saveBatch(List<DishFlavor> flavors);
    /**
     * 批量删除口味数据
     * @return: void
     **/
    void delete(List<Long> ids);
    /**
     * 通过菜品id查询口味
     * @return: List<DishFlavor>
     **/
    @Select("select * from dish_flavor where dish_id = #{id}")
    List<DishFlavor> getFlavorsById(Long id);

    /**
     * 修改与菜品关联的口味
     * @return: void
     **/
//    @AutoFill(value = OperationType.UPDATE)
//    void update(List<DishFlavor> flavors);
    /**
     * 根据id删除口味数据
     * @return: void
     **/
    @Delete("delete from dish_flavor where dish_id = #{id}")
    void deleteById(Long id);
}

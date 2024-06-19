package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotations.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param: id
     * @return: Integer
     **/
    @Select("select count(id) from dish where category_id = #{category_id}")
    Integer countByCategoryId(Long id);
    /**
     * 根据条件查询菜品信息
     * @return: Page<DishVO>
     **/
    Page<DishVO> PageQuery(DishPageQueryDTO dishPageQueryDTO);
    /**
     * 新增菜品信息
     * @return: void
     **/
    @AutoFill(value = OperationType.INSERT)
    void save(Dish dish);

    /**
     * 批量删除菜品
     * @return: void
     **/
    void delete(List<Long> ids);
    /**
     * 根据id查询菜品信息
     * @return: void
     **/
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);
    /**
     *
     * @param: id
     * @return: Dish
     **/
//    Dish getDishVOById(Long id);
    /**
     * 修改菜品
     * @return: void
     **/
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 菜品起售、停售
     * @return: void
     **/
    @Update("update dish set status = #{status} where id = #{id}")
    void setStatus(Integer status, Long id);
}

package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotations.AutoFill;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SetmealMapper {


    /**
     * 查询所有的套餐数据
     * @return: Page<SetmealVO>
     **/
    Page<SetmealVO> list(SetmealPageQueryDTO setmealPageQueryDTO);
    /**
     * 根据分类id查询套餐数量
     * @param: id
     * @return: Integer
     **/
    @Select("select count(id) from setmeal where category_id = #{id}")
    Integer countByCategoryId(Long id);


    /**
     * 根据分类id 查询套餐
     * @return: List<Dish>
     **/

    List<Setmeal> querySetmealByCategoryId(Setmeal setmeal);


    /**
     * 根据套餐 id 查询包含的菜品
     * @return: List<DishItemVO>
     **/
    List<DishItemVO> getDishItemVOById(Long id);

    /**
     * 新增套餐
     * @return: void
     **/
    @AutoFill(OperationType.INSERT)
    void save(Setmeal setmeal);


    /**
     * 插入套餐与菜品关联数据
     * @return: void
     **/

    void saveSetmealWithDish(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐 id 查询数据
     * @return: Setmeal
     **/
    @Select("select * from setmeal where id = #{id}")
    Setmeal querySetmealById(Long id);

    /**
     * 修改套餐
     * @return: void
     **/
    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    /**
     * 根据套餐 id 删除关联菜品
     * @return: void
     **/
    @Delete("delete from setmeal_dish where setmeal_id = #{id}")
    void delete(Long id);
    /**
     * 停售、起售套餐
     * @return: void
     **/
    @Update("update setmeal set status = #{status} where id = #{id}")
    void setStatus(Integer status, Long id);
    /**
     * 根据 id 查询套餐
     * @return: Setmeal
     **/
    @Select("select * from setmeal where id = #{id}")
    Setmeal getSetmealById(Long id);

    /**
     * 批量删除套餐
     * @return: void
     **/
    void deleteSetmealsByIds(List<Long> ids);
}

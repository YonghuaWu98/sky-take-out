package com.sky.mapper;

import com.sky.entity.*;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {
    /**
     * 根据openid查询用户
     * @return: User
     **/
    @Select("select * from  user where openid=#{openid}")
    User getByOpenid(String openid);

    void insert(User user);
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);


//    /**
//     * 根据分类 id 查询菜品
//     * @return: List<Dish>
//     **/
//    @Select("select * from dish where category_id = #{categoryId}")
//    List<Dish> getDishById(Long categoryId);


}

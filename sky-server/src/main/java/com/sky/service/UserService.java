package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.User;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;

import java.util.List;

public interface UserService {
    /**
     * 微信登录
     * @return: User
     **/

    User wxLogin(UserLoginDTO userLoginDTO);





}

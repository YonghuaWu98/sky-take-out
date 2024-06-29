package com.sky.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.support.json.JSONUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.DishRedisKeyConstant;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.sky.constant.DishRedisKeyConstant.CACHE_DISH_KEY;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Resource
    private DishMapper dishMapper;
    @Resource
    private DishFlavorMapper dishFlavorMapper;
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private SetmealDishMapper setmealDishMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 菜品分页查询
     * @param: dishPageQueryDTO
     * @return: PageResult
     **/
    public PageResult queryByPage(DishPageQueryDTO dishPageQueryDTO) {

        //开始分页查询
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> dishes = dishMapper.PageQuery(dishPageQueryDTO);

        return new PageResult(dishes.getTotal(), dishes);

    }
    /**
     * 新增菜品和对应的口味
     * @param: dishDTO
     **/

    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.save(dish);
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() >  0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            //向口味表插入n条数据
            dishFlavorMapper.saveBatch(flavors);
        }
        //删除该菜品对应菜品类别的缓存数据
        String key = CACHE_DISH_KEY + dishDTO.getCategoryId();
        stringRedisTemplate.delete(key);
    }

    /**
     * 批量删除菜品(可以一次删除一个菜品，也可以批量删除菜品）
     * @param: batchId
     * @return: void
     **/
    @Transactional
    public void deleteBatch(List<Long> ids) {
        Set<Long> categoryIds = new HashSet<Long>();
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            categoryIds.add(dish.getCategoryId());
            if (dish.getStatus() == StatusConstant.ENABLE) {
                //当前菜品处于起售中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //被套餐关联的菜品不能删除
        List<Long> setmeals = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmeals != null && setmeals.size() > 0) {
            //当前菜品被套餐关联了，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //逐个或批量删除菜品，在售的菜品不能删除
        dishMapper.delete(ids);
        //批量删除菜品关联口味数据
        dishFlavorMapper.delete(ids);
        //从缓存中删除这些菜品
        cleanCacheDish(CACHE_DISH_KEY + "*");
    }

    /**
     * 根据id查询菜品和对应的口味数据
     * @return: Dish
     **/
    public DishVO getDishWithFlavorById(Long id) {

        //根据id查询菜品
        Dish dish = dishMapper.getById(id);
        //根据id查询口味
        List<DishFlavor> flavors = dishFlavorMapper.getFlavorsById(id);

        DishVO dishVO = new DishVO();
        //将查询到的 dish 数据封装到 DishVO
        BeanUtils.copyProperties(dish, dishVO);
        //将查询到的口味数据封装到 DishVO
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * 修改菜品
     * @return: void
     **/
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {

        //修改菜品表数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        //修改口味信息
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //先删除后插入
        dishFlavorMapper.deleteById(dishDTO.getId());
        if (flavors != null && flavors.size() >  0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            //向口味表插入n条数据
            dishFlavorMapper.saveBatch(flavors);
        }
        //删除缓存(可能会修改菜品分类，会影响多个分类的缓存数据时，选择删除所有菜品分类的缓存。
        //key
        cleanCacheDish(CACHE_DISH_KEY + "*");
    }

    /**
     * 菜品起售、停售
     * @return: void
     **/
    public void setStatus(Integer status, Long id) {

        dishMapper.setStatus(status, id);
        //删除缓存,因为需要查询到当前菜品的分类 id，所以直接删除所有菜品缓存
        //key
        cleanCacheDish(CACHE_DISH_KEY + "*");
    }

    /**
     * 根据分类 id 查询菜品和口味数据
     * @return: List<DishVO>
     **/
    public List<DishVO> listWithFlavor(Dish dish) {
        //1 根据分类 id 从 redis 中获取菜品
        String dish_key = CACHE_DISH_KEY + dish.getCategoryId();
        String cacheDish = stringRedisTemplate.opsForValue().get(dish_key);

        //2 判断是否存在
        if (StrUtil.isNotBlank(cacheDish)) {
            log.info("缓存命中");
            //3 redis 缓存命中
            //JSON 转为对象
            JSONArray dishVOes = JSONUtil.parseArray(cacheDish);
            List<DishVO> dishVO = JSONUtil.toList(dishVOes, DishVO.class);

            return dishVO;
        }
        //4 未命中，从数据库中获取
        log.info("缓存未命中");
        List<DishVO> dishVO = dishMapper.queryDishesById(dish);
        for (DishVO dv : dishVO) {
            List<DishFlavor> dishFlavors = dishFlavorMapper.getFlavorsById(dv.getId());
            dv.setFlavors(dishFlavors);
        }

        //5 添加缓存到数据库并设置过期时间
        stringRedisTemplate.opsForValue().set(dish_key, JSONUtil.toJsonStr(dishVO), 24L, TimeUnit.HOURS);
        return dishVO;
    }

    /**
     * 根据分类id查询菜品
     * @return: List<Dish>
     **/
    public List<Dish> listByCategoryId(DishDTO dishDTO) {
        List<Dish> dishes = dishMapper.list(dishDTO);
        return dishes;
    }

    /**
     * 菜品一旦修改就删除 reids 中对应的缓存
     * @return: void
     **/
    public void cleanCacheDish(String pattern) {
        Set<String> keys = stringRedisTemplate.keys(pattern);
        stringRedisTemplate.delete(keys);
    }

}

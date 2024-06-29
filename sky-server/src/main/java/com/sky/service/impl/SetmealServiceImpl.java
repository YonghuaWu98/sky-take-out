package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Resource
    private SetmealMapper setmealMapper;

    @Resource
    private SetmealDishMapper setmealDishMapper;

    /**
     * 分页查询套餐数据
     * @return: PageResult
     **/
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        //开始分页
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        ////获取所有Setmeal数据
        Page<SetmealVO> setmeals = setmealMapper.list(setmealPageQueryDTO);

        //返回数据
        PageResult pageResult = new PageResult();
        pageResult.setTotal(setmeals.getTotal());
        pageResult.setRecords(setmeals);

        return pageResult;
    }

    /**
     * 新增套餐
     * @return: void
     **/
    @Transactional
    public void add(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //默认为启用
        setmeal.setStatus(StatusConstant.ENABLE);
        setmealMapper.save(setmeal);
        Long setmealId = setmeal.getId();
        //插入套餐与菜品数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            setmealDishes.forEach(
                    setmealDish -> {setmealDish.setSetmealId(setmealId);
            });

        }
        //批量插入
        setmealMapper.saveSetmealWithDish(setmealDishes);
    }
    /**
     * 根据 id 查询套餐
     * @param: id 套餐id
     * @return: SetmealVO
     **/
    public SetmealVO querySetmealById(Long id) {
        SetmealVO setmealVO = new SetmealVO();
        Setmeal setmeal = setmealMapper.querySetmealById(id);
        BeanUtils.copyProperties(setmeal, setmealVO);
        //通过套餐 id 查询套餐与菜品关系
        List<SetmealDish> setmealDishes = setmealDishMapper.queryById(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 修改套餐
     * @return: void
     **/
    @Transactional
    public void updateSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //修改套餐
        setmealMapper.update(setmeal);
        //修改套餐和菜品关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //先删除后插入
        if (setmealDishes != null && setmealDishes.size() > 0) {
            for (SetmealDish setmealDish : setmealDishes) {
                setmealDish.setSetmealId(setmeal.getId());
            }
            setmealMapper.delete(setmeal.getId());
        }
        //保存套餐与菜品关系
        setmealMapper.saveSetmealWithDish(setmealDishes);
    }

    /**
     * 起售、停售套餐
     * @param: status
     * @param: id
     * @return: void
     **/
    public void setStatus(Integer status, Long id) {
        setmealMapper.setStatus(status, id);
    }

    /**
     * 批量删除套餐
     * @return: void
     **/
    @Transactional
    public void delete(List<Long> ids) {
        //1删除套餐
        //1.1 在售的套餐不能删除
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getSetmealById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //1.2 删除套餐
        setmealMapper.deleteSetmealsByIds(ids);
        //2.删除套餐和菜品关系
        setmealDishMapper.deleteSetmealWithDish(ids);
    }


    /**
     * 根据分类 id 查询套餐数据
     * @return: List<Setmeal>
     **/
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> setmeals = setmealMapper.querySetmealByCategoryId(setmeal);
        return setmeals;
    }

    /**
     * 根据套餐 id 查询包含的菜品
     * @return: List<DishItemVO>
     **/

    public List<DishItemVO> queryIncludeDishes(Long id) {
        List<DishItemVO> dishItemVOS = setmealMapper.getDishItemVOById(id);
        return dishItemVOS;
    }


}

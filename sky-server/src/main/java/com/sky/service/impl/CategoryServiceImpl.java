package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static com.sky.constant.MessageConstant.CATEGORY_BE_RELATED_BY_DISH;
import static com.sky.constant.MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL;
import static com.sky.constant.StatusConstant.DISABLE;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private DishMapper dishMapper;

    @Resource
    private SetmealMapper setmealMapper;

    /**
     * 分类分页查询
     * @param: categoryPageQueryDTO
     * @return: PageResult
     **/
    @Override
    public PageResult queryCategoryByPage(CategoryPageQueryDTO categoryPageQueryDTO) {
        //获取请求的页和页大小
        int page = categoryPageQueryDTO.getPage();
        int pageSize = categoryPageQueryDTO.getPageSize();
        //使用mybatis提供的PageHelper进行分页查询
        PageHelper.startPage(page, pageSize);
        //获取所有Category结果
        Page<Category> categories = categoryMapper.pageQuery(categoryPageQueryDTO);
        PageResult pageResult = new PageResult();
        pageResult.setTotal(categories.getTotal());
        pageResult.setRecords(categories);
        return pageResult;
    }
    /**
     * 修改分类
     * @param: categoryDTO
     * @return: void
     **/
    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.update(category);
    }
    /**
     * 启用、禁用分类
     * @param: status
     * @param: id
     * @return: void
     **/
    @Override
    public void updateStatus(Integer status, Long id) {
        categoryMapper.updateStatus(status, id);
    }
    /**
     * 新增分类
     * @param: categoryDTO
     * @return: void
     **/
    @Override
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setCreateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setStatus(DISABLE);//默认为禁用
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.save(category);
    }
    /**
     * 根据id删除分类
     * @return: void
     **/
    @Override
    public void deleteCategoryById(Long id) {
        //删除之前要查询关联分类的的菜品或套餐的数量，数量大于 0 则不能删除
        Integer count = dishMapper.countByCategoryId(id);
        if (count > 0) {
            throw new DeletionNotAllowedException(CATEGORY_BE_RELATED_BY_DISH);
        }
        count = setmealMapper.countByCategoryId(id);
        if (count > 0) {
            throw new DeletionNotAllowedException(CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        categoryMapper.deleteCategoryById(id);
    }

}

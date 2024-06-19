package com.sky.service;

import com.github.pagehelper.Page;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {




    /*
     * 分页查询菜品分类
     * @param: categoryPageQueryDTO
     * @return: PageResult
     **/
    PageResult queryCategoryByPage(CategoryPageQueryDTO categoryPageQueryDTO);
    /**
     * 修改分类
     * @param: categoryDTO
     * @return: void
     **/
    void update(CategoryDTO categoryDTO);
    /**
     * 启用、禁用分类
     * @return: void
     **/
    void updateStatus(Integer status, Long id);
    /**
     * 添加分类
     * @param: categoryDTO
     * @return: void
     **/
    void save(CategoryDTO categoryDTO);

    /**
     * 根据id删除分类
     * @return: void
     **/
    void deleteCategoryById(Long id);

    /**
     * 根据类型查询分类
     * @return: List<Category>
     **/
    List<Category> list(Integer type);
}


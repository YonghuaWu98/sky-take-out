package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotations.AutoFill;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {

    /**
     * 分类分页查询
     * @param: categoryPageQueryDTO
     * @return: Page<Category>
     **/
    Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);
    /**
     * 分类修改
     * @param: category
     * @return: void
     **/
    @AutoFill(value = OperationType.UPDATE)
    void update(Category category);
    /**
     * 启用、禁用分类
     * @return: void
     **/
    void updateStatus(Integer status, Long id);
    /**
     * 新增分类
     * @param: category
     * @return: void
     **/
    @AutoFill(value = OperationType.INSERT)
    void save(Category category);

    /**
     * 根据id删除分类
     * @return: void
     **/
    void deleteCategoryById(Long id);


    /**
     * 按照分类类型查询分类
     * @return: List<Category>
     **/
    List<Category> queryByType(Integer type);
}


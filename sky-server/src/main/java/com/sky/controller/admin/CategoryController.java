package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 分类管理
 */
@RestController("adminCategoryController")
@RequestMapping("/admin/category")
@Slf4j
@Api(tags = "分类相关接口")
public class CategoryController {
    @Resource
    private CategoryService categoryService;

    @GetMapping("/page")
    @ApiOperation(value ="分类分页查询")
    public Result<PageResult> queryByPage(CategoryPageQueryDTO categoryPageQueryDTO) {
        log.info("当前请求数据为{}", categoryPageQueryDTO);
        PageResult pageResult = categoryService.queryCategoryByPage(categoryPageQueryDTO);
        return Result.success(pageResult);
    }
    @PutMapping()
    @ApiOperation(value ="分类修改")
    public Result updateCategory(@RequestBody CategoryDTO categoryDTO) {
        categoryService.update(categoryDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation(value ="启用、禁用分类")
    public Result updateStatus(@PathVariable Integer status, Long id) {
        categoryService.updateStatus(status, id);
        return Result.success();
    }

    @PostMapping()
    @ApiOperation(value ="新增分类")
    public Result save(@RequestBody CategoryDTO categoryDTO) {
        categoryService.save(categoryDTO);
        return Result.success();
    }

    @DeleteMapping()
    @ApiOperation(value ="根据id删除分类")
    public Result deleteCategoryById(@RequestParam Long id) {
        categoryService.deleteCategoryById(id);
        return Result.success();
    }


    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    @GetMapping("/list")
    @ApiOperation(value ="根据类型查询分类")
    public Result<Object> list(Integer type){
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }
}

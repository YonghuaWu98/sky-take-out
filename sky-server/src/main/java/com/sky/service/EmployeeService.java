package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.entity.User;

import java.util.List;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 添加员工
     * @param employeeDTO
     * @return int
     **/
    int addEmployee(EmployeeDTO employeeDTO);
    /*
     * 查询所有的员工信息
     * @return List<User>
     **/
    List<Employee> queryEmployeeByPage(EmployeePageQueryDTO employeePageQueryDTO);
}

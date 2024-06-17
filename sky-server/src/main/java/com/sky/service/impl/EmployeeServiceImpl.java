package com.sky.service.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.entity.User;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static com.sky.constant.PasswordConstant.DEFAULT_PASSWORD;
import static com.sky.constant.StatusConstant.ENABLE;
import static com.sky.context.BaseContext.threadLocal;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Resource
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 进行md5加密，然后再进行比对
        String pwdEncrypted = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!pwdEncrypted.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public int addEmployee(EmployeeDTO employeeDTO) {
        //判断用户名是否重复

        Employee employee = new Employee();
        //对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);
        //设置密码
        employee.setPassword(DigestUtils.md5DigestAsHex(DEFAULT_PASSWORD.getBytes()));
        //设置帐号状态
        employee.setStatus(ENABLE);

        //设置当前记录的创建时间和修改时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());

        //设置当前记录创建人id和修改人id
//        Long userId = BaseContext.getCurrentId();
//        if (userId != null) {
//            employee.setCreateUser(userId);
//            employee.setUpdateUser(userId);
//        }
        return employeeMapper.addEmployee(employee);
    }
    /*
    分页查询
     */
    @Override
    public List<Employee> queryEmployeeByPage(EmployeePageQueryDTO employeePageQueryDTO) {
        //获取员工姓名
        String name = employeePageQueryDTO.getName();
        //获取当前请求页
        int page = employeePageQueryDTO.getPage();
        //获取当前请求页的大小
        int pageSize = employeePageQueryDTO.getPageSize();
        //起始页 page  页的大小 pageSize
        List<Employee> employees = employeeMapper.queryEmployeeByPage(name,(page - 1) * pageSize, pageSize);
        return employees;
    }
    /**
     * 禁用、启用员工帐号
     * @param: status
     * @param: id
     * @return: void
     **/
    @Override
    public void setStatusById(Integer status, Long id) {
        employeeMapper.setStatusById(status, id);
    }

    /**
     * 编辑员工信息
     * @param: employeeDTO
     * @return: void
     **/
    @Override
    public void editEmployee(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工
     * @param: id
     * @return: Employee
     **/
    @Override
    public Employee queryEmployeeById(Long id) {
        return employeeMapper.queryEmployeeById(id);
    }

}

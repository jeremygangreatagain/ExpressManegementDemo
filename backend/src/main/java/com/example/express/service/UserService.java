package com.example.express.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.express.entity.User;

import java.util.Map;

/**
 * 用户服务接口，提供用户相关的业务逻辑
 */
public interface UserService extends IService<User> {

    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);

    /**
     * 检查用户名是否已存在
     */
    boolean existsByUsername(String username);

    /**
     * 保存用户
     */
    void saveUser(User user);

    /**
     * 更新用户信息
     */
    void updateUser(User user);

    /**
     * 生成图形验证码
     * @return 包含验证码图片Base64和验证码Key的Map
     */
    Map<String, String> generateCaptcha();

    /**
     * 验证图形验证码
     * @param captchaKey 验证码Key
     * @param captchaCode 用户输入的验证码
     * @return 验证结果
     */
    boolean validateCaptcha(String captchaKey, String captchaCode);
    
    /**
     * 分页查询用户列表
     * @param page 分页参数
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    Page<User> page(Page<User> page, QueryWrapper<User> queryWrapper);
    
    /**
     * 根据ID获取用户
     * @param id 用户ID
     * @return 用户对象
     */
    User getById(Long id);
    
    /**
     * 根据ID删除用户
     * @param id 用户ID
     * @return 是否删除成功
     */
    boolean removeById(Long id);
}
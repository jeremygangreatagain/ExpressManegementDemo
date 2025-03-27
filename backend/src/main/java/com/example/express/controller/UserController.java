package com.example.express.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.express.annotation.OperationLogAnnotation;
import com.example.express.common.ApiResponse;
import com.example.express.entity.User;
import com.example.express.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器，处理用户的CRUD操作
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取用户列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse getUserList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "role", required = false) String role) {

        // 构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            queryWrapper.like("username", username);
        }
        if (role != null && !role.isEmpty()) {
            queryWrapper.eq("role", role);
        }

        // 按创建时间降序排序
        queryWrapper.orderByDesc("created_at");

        // 分页查询
        Page<User> pageResult = userService.page(new Page<>(page, size), queryWrapper);

        // 返回结果
        Map<String, Object> data = new HashMap<>();
        data.put("total", pageResult.getTotal());
        data.put("list", pageResult.getRecords());

        return ApiResponse.success(data);
    }

    /**
     * 创建用户
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLogAnnotation(operationType = "用户管理")
    public ApiResponse createUser(@RequestBody User user) {
        // 检查用户名是否已存在
        if (userService.existsByUsername(user.getUsername())) {
            return ApiResponse.error("用户名已存在");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置默认状态
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        // 保存用户
        userService.saveUser(user);

        return ApiResponse.success("用户创建成功");
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLogAnnotation(operationType = "用户管理")
    public ApiResponse updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, Integer> statusMap) {

        Integer status = statusMap.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return ApiResponse.error("状态值无效");
        }

        // 获取用户
        User user = userService.getById(userId);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }

        // 更新状态
        user.setStatus(status);
        userService.updateUser(user);

        return ApiResponse.success("用户状态更新成功");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLogAnnotation(operationType = "用户管理")
    public ApiResponse deleteUser(@PathVariable Long userId) {
        // 获取用户
        User user = userService.getById(userId);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }

        // 不允许删除管理员账号
        if (user.isAdmin()) {
            return ApiResponse.error("不能删除管理员账号");
        }

        // 删除用户
        boolean result = userService.removeById(userId);
        if (result) {
            return ApiResponse.success("用户删除成功");
        } else {
            return ApiResponse.error("用户删除失败");
        }
    }
}
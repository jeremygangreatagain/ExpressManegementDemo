package com.example.express.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.express.entity.Order;
import com.example.express.entity.User;
import com.example.express.mapper.OrderMapper;
import com.example.express.service.OrderService;
import com.example.express.service.UserService;
import com.example.express.service.OrderStatusLogService;
import com.example.express.service.OperationLogService;
import com.example.express.entity.OperationLog;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单服务实现类
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderStatusLogService orderStatusLogService;

    @Autowired
    private OperationLogService operationLogService;

    @Override
    public Order createOrder(Order order, String username) {
        // 获取用户信息
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 设置订单初始状态和创建者
        order.setStatus(0); // 0-已创建
        order.setCreatedBy(user.getUserId());
        order.setCreatedAt(LocalDateTime.now());

        // 生成订单ID（实际项目中可能需要更复杂的ID生成策略）
        long orderId = System.currentTimeMillis();
        order.setOrderId(orderId);

        // 保存订单
        orderMapper.insert(order);

        return order;
    }

    @Override
    public List<Order> getOrdersByUsername(String username) {
        // 获取用户信息
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 查询该用户创建的所有订单
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("created_by", user.getUserId());
        queryWrapper.orderByDesc("created_at"); // 按创建时间降序排列

        return orderMapper.selectList(queryWrapper);
    }

    @Override
    public Order getOrderDetail(Long orderId, String username) {
        // 获取用户信息
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return null;
        }

        // 检查权限：只有订单创建者、管理员或员工可以查看订单详情
        if (order.getCreatedBy().equals(user.getUserId()) ||
                "admin".equalsIgnoreCase(user.getRole()) ||
                "staff".equalsIgnoreCase(user.getRole())) {
            return order;
        }

        // 无权查看
        return null;
    }

    @Override
    public boolean updateOrderStatus(Long orderId, Integer status) {
        // 查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }

        // 获取当前操作人信息
        String operator = SecurityContextHolder.getContext().getAuthentication().getName();
        if (operator == null || operator.isEmpty()) {
            return false;
        }

        // 获取当前用户ID
        User user = userService.findByUsername(operator);
        if (user == null) {
            return false;
        }

        // 记录订单状态变更日志
        orderStatusLogService.createStatusLog(orderId, order.getStatus(), status, user.getUserId(),
                order.getCurrentStoreId());

        // 记录操作日志
        OperationLog operationLog = new OperationLog();
        operationLog.setOperatorId(user.getUserId());
        operationLog.setOperationType("更新状态");
        operationLog.setTargetId(orderId);
        operationLog.setDetail("订单状态从" + order.getStatus() + "变更为" + status);
        operationLog.setCreatedAt(LocalDateTime.now());
        operationLogService.save(operationLog);

        // 更新状态
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());

        int result = orderMapper.updateById(order);
        return result > 0;
    }

    @Override
    public Map<String, Object> getAllOrders(String keyword, Integer page, Integer size) {
        // 创建查询条件
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like("receiver_name", keyword)
                    .or()
                    .like("receiver_phone", keyword)
                    .or()
                    .like("receiver_address", keyword)
                    .or()
                    .like("sender_name", keyword)
                    .or()
                    .like("sender_phone", keyword)
                    .or()
                    .like("sender_address", keyword));
        }
        queryWrapper.orderByDesc("created_at"); // 按创建时间降序排列

        // 分页查询
        Page<Order> pageParam = new Page<>(page, size);
        Page<Order> pageResult = orderMapper.selectPage(pageParam, queryWrapper);

        // 封装结果
        Map<String, Object> result = new HashMap<>();
        result.put("orders", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("pages", pageResult.getPages());
        result.put("current", pageResult.getCurrent());
        result.put("size", pageResult.getSize());

        return result;
    }

    @Override
    public boolean deleteOrder(Long orderId) {
        // 查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }

        // 只允许删除已创建状态的订单
        if (order.getStatus() > 0) {
            return false;
        }

        // 删除订单
        int result = orderMapper.deleteById(orderId);
        return result > 0;
    }

    @Override
    public Map<String, Object> getOrderStats() {
        Map<String, Object> stats = new HashMap<>();

        // 获取总订单数
        long totalOrders = count();
        stats.put("totalOrders", totalOrders);

        // 获取各状态订单数量
        for (int status = 0; status <= 4; status++) {
            QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", status);
            long count = count(queryWrapper);
            stats.put("status" + status + "Count", count);
        }

        // 获取今日订单数
        QueryWrapper<Order> todayQuery = new QueryWrapper<>();
        todayQuery.ge("created_at", LocalDateTime.now().toLocalDate().atStartOfDay());
        long todayOrders = count(todayQuery);
        stats.put("todayOrders", todayOrders);

        return stats;
    }

    @Override
    public List<Order> getRecentOrders() {
        // 查询最近10个订单
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created_at");
        Page<Order> page = new Page<>(1, 10);
        Page<Order> pageResult = orderMapper.selectPage(page, queryWrapper);

        return pageResult.getRecords();
    }
}
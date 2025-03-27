package com.example.express.controller;

import com.example.express.annotation.OperationLogAnnotation;
import com.example.express.common.ApiResponse;
import com.example.express.entity.Order;
import com.example.express.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器，处理订单相关请求
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping
    @OperationLogAnnotation(operationType = "创建订单")
    public ApiResponse createOrder(@RequestBody Order order) {
        if (order == null) {
            return ApiResponse.badRequest("订单信息不能为空");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Order createdOrder = orderService.createOrder(order, username);
        return ApiResponse.success("订单创建成功", createdOrder);
    }

    /**
     * 获取当前用户的订单列表
     */
    @GetMapping("/my")
    public ApiResponse getMyOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        List<Order> orders = orderService.getOrdersByUsername(username);
        return ApiResponse.success(orders);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public ApiResponse getOrderDetail(@PathVariable Long orderId) {
        if (orderId == null) {
            return ApiResponse.badRequest("订单ID不能为空");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Order order = orderService.getOrderDetail(orderId, username);
        if (order == null) {
            return ApiResponse.notFound("订单不存在或无权查看");
        }
        return ApiResponse.success(order);
    }

    /**
     * 更新订单状态（仅管理员和员工可操作）
     */
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @OperationLogAnnotation(operationType = "更新订单状态")
    public ApiResponse updateOrderStatus(@PathVariable Long orderId, @RequestBody Map<String, String> statusMap) {
        if (orderId == null) {
            return ApiResponse.badRequest("订单ID不能为空");
        }

        String statusStr = statusMap.get("status");
        if (statusStr == null) {
            return ApiResponse.badRequest("缺少状态参数");
        }

        Integer targetStatus;
        try {
            targetStatus = Integer.parseInt(statusStr);
        } catch (NumberFormatException e) {
            return ApiResponse.badRequest("状态参数格式错误");
        }

        boolean success = orderService.updateOrderStatus(orderId, targetStatus);
        if (!success) {
            return ApiResponse.error("订单状态更新失败");
        }
        return ApiResponse.success("订单状态更新成功");
    }

    /**
     * 获取所有订单（仅管理员和员工可操作）
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ApiResponse getAllOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = orderService.getAllOrders(keyword, page, size);
        return ApiResponse.success(result);
    }

    /**
     * 删除订单（仅管理员可操作）
     */
    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLogAnnotation(operationType = "删除订单")
    public ApiResponse deleteOrder(@PathVariable Long orderId) {
        if (orderId == null) {
            return ApiResponse.badRequest("订单ID不能为空");
        }

        boolean success = orderService.deleteOrder(orderId);
        if (!success) {
            return ApiResponse.error("订单删除失败");
        }
        return ApiResponse.success("订单删除成功");
    }

    /**
     * 获取订单统计数据（仅管理员和员工可操作）
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ApiResponse getOrderStats() {
        Map<String, Object> stats = orderService.getOrderStats();
        return ApiResponse.success(stats);
    }

    /**
     * 获取最近订单列表（仅管理员和员工可操作）
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ApiResponse getRecentOrders() {
        List<Order> recentOrders = orderService.getRecentOrders();
        return ApiResponse.success(recentOrders);
    }

    /**
     * 获取订单状态选项（供前端下拉菜单使用）
     */
    @GetMapping("/status-options")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ApiResponse getOrderStatusOptions() {
        List<Map<String, Object>> statusOptions = new ArrayList<>();

        // 添加所有可能的订单状态
        Map<String, Object> option0 = new HashMap<>();
        option0.put("value", 0);
        option0.put("label", "已创建");
        statusOptions.add(option0);

        Map<String, Object> option1 = new HashMap<>();
        option1.put("value", 1);
        option1.put("label", "已揽收");
        statusOptions.add(option1);

        Map<String, Object> option2 = new HashMap<>();
        option2.put("value", 2);
        option2.put("label", "运输中");
        statusOptions.add(option2);

        Map<String, Object> option3 = new HashMap<>();
        option3.put("value", 3);
        option3.put("label", "派送中");
        statusOptions.add(option3);

        Map<String, Object> option4 = new HashMap<>();
        option4.put("value", 4);
        option4.put("label", "已签收");
        statusOptions.add(option4);

        return ApiResponse.success(statusOptions);
    }
}
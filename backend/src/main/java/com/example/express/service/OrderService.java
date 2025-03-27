package com.example.express.service;

import com.example.express.entity.Order;

import java.util.List;
import java.util.Map;

/**
 * 订单服务接口，提供订单相关的业务逻辑
 */
public interface OrderService {

    /**
     * 创建订单
     * 
     * @param order    订单信息
     * @param username 创建者用户名
     * @return 创建后的订单
     */
    Order createOrder(Order order, String username);

    /**
     * 根据用户名获取订单列表
     * 
     * @param username 用户名
     * @return 订单列表
     */
    List<Order> getOrdersByUsername(String username);

    /**
     * 获取订单详情
     * 
     * @param orderId  订单ID
     * @param username 查询用户名
     * @return 订单详情
     */
    Order getOrderDetail(Long orderId, String username);

    /**
     * 更新订单状态
     * 
     * @param orderId 订单ID
     * @param status  目标状态
     * @return 更新结果
     */
    boolean updateOrderStatus(Long orderId, Integer status);

    /**
     * 获取所有订单（分页）
     * 
     * @param keyword 搜索关键字（可选）
     * @param page    页码
     * @param size    每页大小
     * @return 订单列表和分页信息
     */
    Map<String, Object> getAllOrders(String keyword, Integer page, Integer size);

    /**
     * 删除订单
     * 
     * @param orderId 订单ID
     * @return 删除结果
     */
    boolean deleteOrder(Long orderId);

    /**
     * 获取订单统计数据
     * 
     * @return 统计数据，包含各状态订单数量、总订单数等信息
     */
    Map<String, Object> getOrderStats();

    /**
     * 获取最近订单列表
     * 
     * @return 最近的订单列表
     */
    List<Order> getRecentOrders();
}
package com.example.express.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.express.entity.OrderStatusLog;

public interface OrderStatusLogService extends IService<OrderStatusLog> {
  /**
   * 创建订单状态变更日志
   * 
   * @param orderId    订单ID
   * @param oldStatus  原状态
   * @param newStatus  新状态
   * @param operatorId 操作人ID
   * @param storeId    门店ID
   * @return 创建的日志记录
   */
  OrderStatusLog createStatusLog(Long orderId, Integer oldStatus, Integer newStatus, Long operatorId, Long storeId);
}
package com.example.express.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.express.entity.OrderStatusLog;
import com.example.express.mapper.OrderStatusLogMapper;
import com.example.express.service.OrderStatusLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderStatusLogServiceImpl extends ServiceImpl<OrderStatusLogMapper, OrderStatusLog>
    implements OrderStatusLogService {

  @Autowired
  private OrderStatusLogMapper orderStatusLogMapper;

  @Override
  public OrderStatusLog createStatusLog(Long orderId, Integer oldStatus, Integer newStatus, Long operatorId,
      Long storeId) {
    OrderStatusLog log = new OrderStatusLog();
    log.setOrderId(orderId);
    log.setOldStatus(oldStatus);
    log.setNewStatus(newStatus);
    log.setOperatorId(operatorId);
    log.setStoreId(storeId);
    log.setCreatedAt(LocalDateTime.now());

    orderStatusLogMapper.insert(log);
    return log;
  }
}
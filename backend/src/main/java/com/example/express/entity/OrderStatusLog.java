package com.example.express.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_status_log")
public class OrderStatusLog {
  @TableId(type = IdType.AUTO)
  private Long logId;
  private Long orderId;
  private Integer oldStatus;
  private Integer newStatus;
  private Long storeId;
  private Long operatorId;
  private LocalDateTime createdAt;
}
package com.example.express.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {
  @TableId(type = IdType.AUTO)
  private Long logId;
  private Long operatorId;
  private String operationType;
  private Long targetId;
  private String detail;
  private String ipAddress;
  private LocalDateTime createdAt;
}
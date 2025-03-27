package com.example.express.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.express.entity.OperationLog;
import java.util.Map;

public interface OperationLogService extends IService<OperationLog> {
  /**
   * 创建操作日志
   */
  OperationLog createLog(OperationLog log);

  /**
   * 分页获取操作日志列表
   */
  Map<String, Object> getOperationLogs(Integer page, Integer size, String operationType);
}
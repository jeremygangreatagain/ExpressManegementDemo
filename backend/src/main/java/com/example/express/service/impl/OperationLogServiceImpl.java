package com.example.express.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.express.entity.OperationLog;
import com.example.express.mapper.OperationLogMapper;
import com.example.express.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog>
    implements OperationLogService {

  @Autowired
  private OperationLogMapper operationLogMapper;

  @Override
  public OperationLog createLog(OperationLog log) {
    log.setCreatedAt(LocalDateTime.now());
    operationLogMapper.insert(log);
    return log;
  }

  @Override
  public Map<String, Object> getOperationLogs(Integer page, Integer size, String operationType) {
    QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();
    if (operationType != null && !operationType.isEmpty()) {
      queryWrapper.eq("operation_type", operationType);
    }
    queryWrapper.orderByDesc("created_at");

    Page<OperationLog> pageParam = new Page<>(page, size);
    Page<OperationLog> pageResult = operationLogMapper.selectPage(pageParam, queryWrapper);

    Map<String, Object> result = new HashMap<>();
    result.put("total", pageResult.getTotal());
    result.put("logs", pageResult.getRecords());
    return result;
  }
}
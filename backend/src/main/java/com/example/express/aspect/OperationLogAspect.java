package com.example.express.aspect;

import com.example.express.annotation.OperationLogAnnotation;
import com.example.express.entity.OperationLog;
import com.example.express.entity.User;
import com.example.express.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
public class OperationLogAspect {

  @Autowired
  private OperationLogService operationLogService;

  @Pointcut("@annotation(com.example.express.annotation.OperationLogAnnotation)")
  public void operationLogPointCut() {
  }

  @AfterReturning(pointcut = "operationLogPointCut()")
  public void saveOperationLog(JoinPoint joinPoint) {
    // 获取当前登录用户
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return;
    }

    // 获取请求IP
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes == null) {
      return;
    }
    HttpServletRequest request = attributes.getRequest();
    String ipAddress = request.getRemoteAddr();

    // 获取注解信息
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    OperationLogAnnotation annotation = method.getAnnotation(OperationLogAnnotation.class);

    // 创建操作日志
    OperationLog log = new OperationLog();
    Object principal = authentication.getPrincipal();
    if (principal instanceof User) {
      log.setOperatorId(((User) principal).getId());
    } else {
      return; // 如果无法获取用户信息，不记录日志
    }
    log.setOperationType(annotation.operationType());
    log.setIpAddress(ipAddress);

    // 获取目标ID（如果有）
    Object[] args = joinPoint.getArgs();
    if (args.length > 0 && args[0] instanceof Long) {
      log.setTargetId((Long) args[0]);
    }

    // 保存日志
    operationLogService.createLog(log);
  }
}
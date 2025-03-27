package com.example.express.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("express_order")
public class Order {
    @TableId(type = IdType.INPUT) // 使用雪花算法生成ID
    private Long orderId;

    private String senderInfo; // JSON格式存储寄件人信息

    private String receiverInfo; // JSON格式存储收件人信息

    private String itemType; // 物品类型：电器、玻璃、陶瓷

    private Long currentStoreId; // 当前处理门店ID

    private Integer status; // 0-已创建，1-已揽收，2-运输中，3-派送中，4-已签收

    private Long createdBy; // 创建用户ID

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public String canTransitTo(Integer targetStatus) {
        // 检查目标状态是否在有效范围内
        if (targetStatus < 0 || targetStatus > 4) {
            return "无效的目标状态：" + targetStatus;
        }

        // 检查是否为相同状态
        if (this.status.equals(targetStatus)) {
            return "当前订单已经处于" + this.getStatusText() + "状态";
        }

        // 允许任意状态之间的转换
        return null;
    }

    public String getStatusText() {
        return switch (status) {
            case 0 -> "已创建";
            case 1 -> "已揽收";
            case 2 -> "运输中";
            case 3 -> "派送中";
            case 4 -> "已签收";
            default -> "未知状态";
        };
    }
}
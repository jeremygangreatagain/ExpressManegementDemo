package com.example.express.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_staff")
public class Staff {
    @TableId(type = IdType.AUTO)
    private Long staffId;

    private String name;

    private String phone;

    private Long storeId;  // 所属门店ID

    private String position;  // 职位

    private Integer status;  // 0-离职，1-在职

    private Long userId;  // 关联的用户ID

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
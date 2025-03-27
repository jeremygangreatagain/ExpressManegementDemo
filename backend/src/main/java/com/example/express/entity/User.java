package com.example.express.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long userId;

    private String username;

    private String password;

    private String phone;

    private String role;

    private Integer status;

    private Long storeId;

    private LocalDateTime lastLogin;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public boolean isEnabled() {
        return status != null && status == 1;
    }

    public boolean isAdmin() {
        return "admin".equals(role);
    }

    public boolean isStaff() {
        return "staff".equals(role);
    }

    public boolean isCustomer() {
        return "customer".equals(role);
    }

    public Long getId() {
        return this.userId;
    }
}
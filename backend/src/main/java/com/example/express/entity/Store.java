package com.example.express.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("store")
public class Store {
    @TableId(type = IdType.AUTO)
    private Long storeId;

    private String name;

    @JsonRawValue
    private String address; // JSON格式存储地址信息

    // 使用字符串存储GPS坐标，格式为"longitude,latitude"
    @TableField(value = "gps_location", typeHandler = com.example.express.config.MySqlPointTypeHandler.class)
    private String gpsLocation = "0.0,0.0"; // 默认初始化为0.0,0.0

    private Integer status; // 0-关闭，1-营业

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public boolean isOperating() {
        return status != null && status == 1;
    }

    // 获取GPS坐标数组
    public double[] getGpsCoordinates() {
        if (gpsLocation == null || gpsLocation.isEmpty()) {
            return new double[] { 0, 0 };
        }
        try {
            String[] parts = gpsLocation.split(",");
            if (parts.length == 2) {
                return new double[] {
                        Double.parseDouble(parts[0]), // 经度
                        Double.parseDouble(parts[1]) // 纬度
                };
            }
        } catch (Exception e) {
            // 解析失败时返回默认值
        }
        return new double[] { 0, 0 };
    }

    // 设置GPS坐标
    public void setGpsCoordinates(double longitude, double latitude) {
        this.gpsLocation = longitude + "," + latitude;
    }
}
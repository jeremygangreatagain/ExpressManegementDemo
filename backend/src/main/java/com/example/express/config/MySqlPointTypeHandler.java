package com.example.express.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;

/**
 * MySQL POINT类型处理器
 * 用于处理Java字符串类型与MySQL POINT类型之间的转换
 * 格式为: "longitude,latitude"
 */
@MappedTypes(String.class)
public class MySqlPointTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        // 将字符串格式的坐标转换为MySQL POINT格式
        // 格式: "longitude,latitude" -> POINT(longitude latitude)
        String[] parts = parameter.split(",");
        if (parts.length != 2) {
            // 如果格式不正确，使用默认值
            parts = new String[]{"0.0", "0.0"};
        }
        
        try {
            // 使用MySQL的ST_GeomFromText函数创建POINT
            String pointWKT = String.format("POINT(%s %s)", parts[0], parts[1]);
            
            // 使用PreparedStatement的方式设置参数，避免SQL注入
            String sql = "SELECT ST_GeomFromText(?)";
            PreparedStatement pstmt = ps.getConnection().prepareStatement(sql);
            pstmt.setString(1, pointWKT);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                ps.setObject(i, rs.getObject(1));
            } else {
                // 如果查询没有结果，使用默认值
                ps.setNull(i, Types.OTHER);
            }
            
            // 关闭资源
            rs.close();
            pstmt.close();
        } catch (Exception e) {
            // 如果转换失败，使用默认值
            ps.setNull(i, Types.OTHER);
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 从ResultSet中获取POINT类型的值并转换为字符串
        try {
            String point = rs.getString(columnName);
            if (point == null) {
                return "0.0,0.0";
            }
            // 解析POINT格式: POINT(longitude latitude)
            // 提取经纬度并转换为"longitude,latitude"格式
            if (point.startsWith("POINT(") && point.endsWith(")")) {
                String coordinates = point.substring(6, point.length() - 1).trim();
                String[] parts = coordinates.split(" ");
                if (parts.length == 2) {
                    return parts[0] + "," + parts[1];
                }
            }
            return "0.0,0.0";
        } catch (Exception e) {
            return "0.0,0.0";
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        try {
            String point = rs.getString(columnIndex);
            if (point == null) {
                return "0.0,0.0";
            }
            if (point.startsWith("POINT(") && point.endsWith(")")) {
                String coordinates = point.substring(6, point.length() - 1).trim();
                String[] parts = coordinates.split(" ");
                if (parts.length == 2) {
                    return parts[0] + "," + parts[1];
                }
            }
            return "0.0,0.0";
        } catch (Exception e) {
            return "0.0,0.0";
        }
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        try {
            String point = cs.getString(columnIndex);
            if (point == null) {
                return "0.0,0.0";
            }
            if (point.startsWith("POINT(") && point.endsWith(")")) {
                String coordinates = point.substring(6, point.length() - 1).trim();
                String[] parts = coordinates.split(" ");
                if (parts.length == 2) {
                    return parts[0] + "," + parts[1];
                }
            }
            return "0.0,0.0";
        } catch (Exception e) {
            return "0.0,0.0";
        }
    }
}
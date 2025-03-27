package com.example.express.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.express.entity.Staff;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工数据访问接口
 */
@Mapper
public interface StaffMapper extends BaseMapper<Staff> {
    // 继承BaseMapper后，已经有基本的CRUD方法
    // 如需自定义复杂查询，可在此添加方法
}
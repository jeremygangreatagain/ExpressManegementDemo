package com.example.express.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.express.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单数据访问接口
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    // 继承BaseMapper后，已经有基本的CRUD方法
    // 如需自定义复杂查询，可在此添加方法
}
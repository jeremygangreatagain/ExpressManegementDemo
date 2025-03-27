package com.example.express.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.express.entity.Store;
import org.apache.ibatis.annotations.Mapper;

/**
 * 门店数据访问接口
 */
@Mapper
public interface StoreMapper extends BaseMapper<Store> {
    // 继承BaseMapper后，基本的CRUD操作已经由MyBatis-Plus提供
    // 如需自定义复杂查询，可在此添加方法
}
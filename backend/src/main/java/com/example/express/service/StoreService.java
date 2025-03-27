package com.example.express.service;

import com.example.express.entity.Store;

import java.util.List;

/**
 * 门店服务接口
 */
public interface StoreService {

    /**
     * 获取所有门店
     */
    List<Store> getAllStores();

    /**
     * 获取门店详情
     */
    Store getStoreDetail(Long storeId);

    /**
     * 创建门店
     */
    Store createStore(Store store);

    /**
     * 更新门店信息
     */
    Store updateStore(Store store);

    /**
     * 删除门店
     */
    boolean deleteStore(Long storeId);
}
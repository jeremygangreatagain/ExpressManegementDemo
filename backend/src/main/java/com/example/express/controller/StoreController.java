package com.example.express.controller;

import com.example.express.common.ApiResponse;
import com.example.express.entity.Store;
import com.example.express.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门店管理控制器
 */
@RestController
@RequestMapping("/api/stores")
public class StoreController {

    @Autowired
    private StoreService storeService;

    /**
     * 获取所有门店
     */
    @GetMapping
    public ApiResponse<List<Store>> getAllStores() {
        List<Store> stores = storeService.getAllStores();
        return ApiResponse.success(stores);
    }

    /**
     * 获取门店详情
     */
    @GetMapping("/{storeId}")
    public ApiResponse<Store> getStoreDetail(@PathVariable Long storeId) {
        Store store = storeService.getStoreDetail(storeId);
        return ApiResponse.success(store);
    }

    /**
     * 创建门店
     */
    @PostMapping
    public ApiResponse<Store> createStore(@RequestBody Store store) {
        Store createdStore = storeService.createStore(store);
        return ApiResponse.success(createdStore);
    }

    /**
     * 更新门店信息
     */
    @PutMapping("/{storeId}")
    public ApiResponse<Store> updateStore(@PathVariable Long storeId, @RequestBody Store store) {
        store.setStoreId(storeId);
        Store updatedStore = storeService.updateStore(store);
        return ApiResponse.success(updatedStore);
    }

    /**
     * 删除门店
     */
    @DeleteMapping("/{storeId}")
    public ApiResponse<Boolean> deleteStore(@PathVariable Long storeId) {
        boolean result = storeService.deleteStore(storeId);
        return ApiResponse.success(result);
    }
}
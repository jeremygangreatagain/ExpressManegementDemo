package com.example.express.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.dao.DataIntegrityViolationException;
import com.example.express.entity.Staff;
import com.example.express.entity.Store;
import com.example.express.mapper.StoreMapper;
import com.example.express.service.StaffService;
import com.example.express.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 门店服务实现类
 */
@Service
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store> implements StoreService {

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private StaffService staffService;

    @Override
    public List<Store> getAllStores() {
        return list();
    }

    @Override
    public Store getStoreDetail(Long storeId) {
        return getById(storeId);
    }

    @Override
    public Store createStore(Store store) {
        // 确保gpsLocation不为null，并且格式正确
        if (store.getGpsLocation() == null || store.getGpsLocation().isEmpty()) {
            // 设置默认的GPS坐标字符串
            store.setGpsLocation("0.0,0.0");
        } else {
            // 确保格式为"经度,纬度"，如果不是，则设置为默认值
            if (!store.getGpsLocation().matches("^-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?$")) {
                store.setGpsLocation("0.0,0.0");
            }
        }

        // 如果address是字符串而非JSON格式，进行转换
        if (store.getAddress() != null && !store.getAddress().startsWith("{")) {
            try {
                // 创建简单的JSON格式地址
                String jsonAddress = String.format(
                        "{\"fullAddress\":\"%s\",\"detailAddress\":\"%s\"}",
                        store.getAddress(), store.getAddress());
                store.setAddress(jsonAddress);
            } catch (Exception e) {
                // 保持原样，避免错误
            }
        }

        // 使用save方法保存门店信息
        boolean saved = save(store);
        if (!saved) {
            return null;
        }
        return store;
    }

    @Override
    public Store updateStore(Store store) {
        // 获取原始门店信息
        Store existingStore = getById(store.getStoreId());
        if (existingStore == null) {
            return null;
        }

        // 确保gpsLocation不为null
        if (store.getGpsLocation() == null) {
            // 如果更新时没有提供gpsLocation，保留原有值
            if (existingStore.getGpsLocation() != null) {
                store.setGpsLocation(existingStore.getGpsLocation());
            } else {
                // 设置默认的GPS坐标字符串
                store.setGpsLocation("0.0,0.0");
            }
        } else {
            // 确保格式为"经度,纬度"，如果不是，则设置为默认值
            if (!store.getGpsLocation().matches("^-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?$")) {
                store.setGpsLocation("0.0,0.0");
            }
        }

        // 如果address是字符串而非JSON格式，进行转换
        if (store.getAddress() != null && !store.getAddress().startsWith("{")) {
            try {
                // 创建简单的JSON格式地址
                String jsonAddress = String.format(
                        "{\"fullAddress\":\"%s\",\"detailAddress\":\"%s\"}",
                        store.getAddress(), store.getAddress());
                store.setAddress(jsonAddress);
            } catch (Exception e) {
                // 保持原样，避免错误
            }
        }

        updateById(store);
        return getById(store.getStoreId());
    }

    @Override
    public boolean deleteStore(Long storeId) {
        // 检查门店下是否存在员工
        LambdaQueryWrapper<Staff> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Staff::getStoreId, storeId);
        long count = staffService.count(queryWrapper);
        if (count > 0) {
            throw new DataIntegrityViolationException("该门店下还有关联的员工，请先处理相关员工后再删除门店");
        }
        return removeById(storeId);
    }
}
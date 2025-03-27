package com.example.express.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.express.entity.Staff;

import java.util.Map;

/**
 * 员工服务接口
 */
public interface StaffService extends IService<Staff> {

    /**
     * 创建员工
     * @param staff 员工信息
     * @return 创建后的员工信息
     */
    Staff createStaff(Staff staff);

    /**
     * 更新员工信息
     * @param staff 员工信息
     * @return 更新后的员工信息
     */
    Staff updateStaff(Staff staff);

    /**
     * 获取员工详情
     * @param staffId 员工ID
     * @return 员工详情
     */
    Staff getStaffDetail(Long staffId);

    /**
     * 获取所有员工（分页）
     * @param page 页码
     * @param size 每页大小
     * @return 员工列表和分页信息
     */
    Map<String, Object> getAllStaffs(Integer page, Integer size);

    /**
     * 删除员工
     * @param staffId 员工ID
     * @return 删除结果
     */
    boolean deleteStaff(Long staffId);

    /**
     * 获取员工统计数据
     * @return 统计数据
     */
    Map<String, Object> getStaffStats();
}
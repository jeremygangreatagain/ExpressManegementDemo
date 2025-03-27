package com.example.express.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.express.entity.Staff;
import com.example.express.mapper.StaffMapper;
import com.example.express.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 员工服务实现类
 */
@Service
public class StaffServiceImpl extends ServiceImpl<StaffMapper, Staff> implements StaffService {

    @Autowired
    private StaffMapper staffMapper;

    @Override
    public Staff createStaff(Staff staff) {
        // 设置初始状态
        staff.setStatus(1); // 1-在职
        staff.setCreatedAt(LocalDateTime.now());
        
        // 保存员工信息
        staffMapper.insert(staff);
        
        return staff;
    }

    @Override
    public Staff updateStaff(Staff staff) {
        // 更新时间
        staff.setUpdatedAt(LocalDateTime.now());
        
        // 更新员工信息
        staffMapper.updateById(staff);
        
        return staff;
    }

    @Override
    public Staff getStaffDetail(Long staffId) {
        return staffMapper.selectById(staffId);
    }

    @Override
    public Map<String, Object> getAllStaffs(Integer page, Integer size) {
        // 创建查询条件
        QueryWrapper<Staff> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created_at"); // 按创建时间降序排列

        // 分页查询
        Page<Staff> pageParam = new Page<>(page, size);
        Page<Staff> pageResult = staffMapper.selectPage(pageParam, queryWrapper);

        // 封装结果
        Map<String, Object> result = new HashMap<>();
        result.put("staffs", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("pages", pageResult.getPages());
        result.put("current", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        
        return result;
    }

    @Override
    public boolean deleteStaff(Long staffId) {
        // 查询员工
        Staff staff = staffMapper.selectById(staffId);
        if (staff == null) {
            return false;
        }

        // 删除员工
        int result = staffMapper.deleteById(staffId);
        return result > 0;
    }

    @Override
    public Map<String, Object> getStaffStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取总员工数
        long totalStaffs = count();
        stats.put("totalStaffs", totalStaffs);
        
        // 获取在职员工数
        QueryWrapper<Staff> activeQuery = new QueryWrapper<>();
        activeQuery.eq("status", 1);
        long activeStaffs = count(activeQuery);
        stats.put("activeStaffs", activeStaffs);
        
        // 获取离职员工数
        QueryWrapper<Staff> inactiveQuery = new QueryWrapper<>();
        inactiveQuery.eq("status", 0);
        long inactiveStaffs = count(inactiveQuery);
        stats.put("inactiveStaffs", inactiveStaffs);
        
        return stats;
    }
}
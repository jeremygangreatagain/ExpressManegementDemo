package com.example.express.controller;

import com.example.express.common.ApiResponse;
import com.example.express.entity.Staff;
import com.example.express.service.StaffService;
import com.example.express.annotation.OperationLogAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 员工管理控制器
 */
@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasRole('ADMIN')")
public class StaffController {

    @Autowired
    private StaffService staffService;

    /**
     * 创建员工
     */
    @PostMapping
    @OperationLogAnnotation(operationType = "创建员工")
    public ApiResponse<Staff> createStaff(@RequestBody Staff staff) {
        Staff createdStaff = staffService.createStaff(staff);
        return ApiResponse.success(createdStaff);
    }

    /**
     * 更新员工信息
     */
    @PutMapping("/{staffId}")
    @OperationLogAnnotation(operationType = "更新员工信息")
    public ApiResponse<Staff> updateStaff(@PathVariable Long staffId, @RequestBody Staff staff) {
        staff.setStaffId(staffId);
        Staff updatedStaff = staffService.updateStaff(staff);
        return ApiResponse.success(updatedStaff);
    }

    /**
     * 获取员工详情
     */
    @GetMapping("/{staffId}")
    public ApiResponse<Staff> getStaffDetail(@PathVariable Long staffId) {
        Staff staff = staffService.getStaffDetail(staffId);
        if (staff == null) {
            return ApiResponse.notFound("员工不存在");
        }
        return ApiResponse.success(staff);
    }

    /**
     * 获取所有员工（分页）
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> getAllStaffs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> staffs = staffService.getAllStaffs(page, size);
        return ApiResponse.success(staffs);
    }

    /**
     * 删除员工
     */
    @DeleteMapping("/{staffId}")
    @OperationLogAnnotation(operationType = "删除员工")
    public ApiResponse<Boolean> deleteStaff(@PathVariable Long staffId) {
        boolean result = staffService.deleteStaff(staffId);
        if (!result) {
            return ApiResponse.error("删除员工失败");
        }
        return ApiResponse.success(result);
    }

    /**
     * 获取员工统计数据
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStaffStats() {
        Map<String, Object> stats = staffService.getStaffStats();
        return ApiResponse.success(stats);
    }
}
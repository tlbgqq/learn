package com.studyagent.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyagent.config.JwtInterceptor;
import com.studyagent.dto.ApiResponse;
import com.studyagent.entity.SysUser;
import com.studyagent.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
@CrossOrigin
public class SysUserController {

    private final SysUserService sysUserService;

    @GetMapping("/list")
    public ApiResponse<Page<SysUser>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        Page<SysUser> page = sysUserService.getUserPage(current, size, username, status, roleId, startTime, endTime);
        return ApiResponse.success(page);
    }

    @GetMapping("/{id}")
    public ApiResponse<SysUser> getById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        user.setPassword(null);
        return ApiResponse.success(user);
    }

    @PostMapping
    public ApiResponse<String> add(@RequestBody SysUser user) {
        try {
            sysUserService.addUser(user);
            return ApiResponse.success("新增成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping
    public ApiResponse<String> update(@RequestBody SysUser user) {
        try {
            sysUserService.updateUser(user);
            return ApiResponse.success("更新成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long currentUserId = (Long) request.getAttribute(JwtInterceptor.USER_ID_HEADER);
            sysUserService.deleteUser(id, currentUserId);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/batch")
    public ApiResponse<String> batchDelete(@RequestBody Map<String, List<Long>> params, HttpServletRequest request) {
        try {
            List<Long> ids = params.get("ids");
            if (ids == null || ids.isEmpty()) {
                return ApiResponse.error("请选择要删除的用户");
            }
            Long currentUserId = (Long) request.getAttribute(JwtInterceptor.USER_ID_HEADER);
            sysUserService.batchDelete(ids, currentUserId);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/roles")
    public ApiResponse<String> assignRoles(@PathVariable Long id, @RequestBody Map<String, Long[]> params) {
        try {
            Long[] roleIds = params.get("roleIds");
            sysUserService.assignRoles(id, roleIds);
            return ApiResponse.success("分配角色成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/reset-password")
    public ApiResponse<String> resetPassword(@PathVariable Long id) {
        try {
            sysUserService.resetPassword(id);
            return ApiResponse.success("密码已重置为: Admin@123456", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/password")
    public ApiResponse<String> updatePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute(JwtInterceptor.USER_ID_HEADER);
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");
            sysUserService.updatePassword(userId, oldPassword, newPassword);
            return ApiResponse.success("密码修改成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}

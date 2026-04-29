package com.studyagent.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyagent.dto.ApiResponse;
import com.studyagent.entity.SysRole;
import com.studyagent.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/role")
@RequiredArgsConstructor
@CrossOrigin
public class SysRoleController {

    private final SysRoleService sysRoleService;

    @GetMapping("/list")
    public ApiResponse<Page<SysRole>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        Page<SysRole> page = sysRoleService.getRolePage(current, size, name, status);
        return ApiResponse.success(page);
    }

    @GetMapping("/all")
    public ApiResponse<List<SysRole>> listAll() {
        List<SysRole> roles = sysRoleService.listAll();
        return ApiResponse.success(roles);
    }

    @GetMapping("/{id}")
    public ApiResponse<SysRole> getById(@PathVariable Long id) {
        SysRole role = sysRoleService.getById(id);
        if (role == null) {
            return ApiResponse.error("角色不存在");
        }
        return ApiResponse.success(role);
    }

    @PostMapping
    public ApiResponse<String> add(@RequestBody SysRole role) {
        try {
            sysRoleService.addRole(role);
            return ApiResponse.success("新增成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping
    public ApiResponse<String> update(@RequestBody SysRole role) {
        try {
            sysRoleService.updateRole(role);
            return ApiResponse.success("更新成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        try {
            sysRoleService.deleteRole(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/menus")
    public ApiResponse<List<Long>> getMenus(@PathVariable Long id) {
        List<Long> menuIds = sysRoleService.getMenuIdsByRoleId(id);
        return ApiResponse.success(menuIds);
    }

    @PutMapping("/{id}/menus")
    public ApiResponse<String> assignMenus(@PathVariable Long id, @RequestBody Map<String, Long[]> params) {
        try {
            Long[] menuIds = params.get("menuIds");
            sysRoleService.assignMenus(id, menuIds);
            return ApiResponse.success("分配菜单权限成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}

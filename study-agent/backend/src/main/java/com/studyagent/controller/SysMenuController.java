package com.studyagent.controller;

import com.studyagent.config.JwtInterceptor;
import com.studyagent.dto.ApiResponse;
import com.studyagent.entity.SysMenu;
import com.studyagent.service.SysMenuService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/menu")
@RequiredArgsConstructor
@CrossOrigin
public class SysMenuController {

    private final SysMenuService sysMenuService;

    @GetMapping("/tree")
    public ApiResponse<List<SysMenu>> getMenuTree() {
        List<SysMenu> menuTree = sysMenuService.getMenuTree();
        return ApiResponse.success(menuTree);
    }

    @GetMapping("/user-tree")
    public ApiResponse<List<SysMenu>> getUserMenuTree(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(JwtInterceptor.USER_ID_HEADER);
        if (userId == null) {
            return ApiResponse.error("未登录");
        }
        List<SysMenu> menuTree = sysMenuService.getUserMenuTree(userId);
        return ApiResponse.success(menuTree);
    }

    @GetMapping("/{id}")
    public ApiResponse<SysMenu> getById(@PathVariable Long id) {
        SysMenu menu = sysMenuService.getById(id);
        if (menu == null) {
            return ApiResponse.error("菜单不存在");
        }
        return ApiResponse.success(menu);
    }

    @PostMapping
    public ApiResponse<String> add(@RequestBody SysMenu menu) {
        try {
            sysMenuService.addMenu(menu);
            return ApiResponse.success("新增成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping
    public ApiResponse<String> update(@RequestBody SysMenu menu) {
        try {
            sysMenuService.updateMenu(menu);
            return ApiResponse.success("更新成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        try {
            sysMenuService.deleteMenu(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/sort")
    public ApiResponse<String> updateSort(@RequestBody List<Map<String, Object>> menuList) {
        try {
            sysMenuService.updateSort(menuList);
            return ApiResponse.success("排序更新成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}

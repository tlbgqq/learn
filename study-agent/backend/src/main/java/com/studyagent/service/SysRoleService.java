package com.studyagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyagent.entity.SysRole;
import com.studyagent.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleService {

    private static final String SUPER_ADMIN_ROLE = "ROLE_SUPER_ADMIN";

    private final SysRoleMapper sysRoleMapper;

    public Page<SysRole> getRolePage(Integer current, Integer size, String name, Integer status) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(name)) {
            wrapper.like(SysRole::getName, name).or().like(SysRole::getCode, name);
        }
        
        if (status != null) {
            wrapper.eq(SysRole::getStatus, status);
        }
        
        wrapper.orderByAsc(SysRole::getSort);
        
        Page<SysRole> page = sysRoleMapper.selectPage(new Page<>(current, size), wrapper);
        
        for (SysRole role : page.getRecords()) {
            role.setUserCount(sysRoleMapper.selectUserCountByRoleId(role.getId()));
        }
        
        return page;
    }

    public List<SysRole> listAll() {
        return sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getDel, 0)
                        .eq(SysRole::getStatus, 1)
                        .orderByAsc(SysRole::getSort)
        );
    }

    public SysRole getById(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role != null) {
            List<Long> menuIds = sysRoleMapper.selectMenuIdsByRoleId(id);
            role.setMenuIds(menuIds.toArray(new Long[0]));
        }
        return role;
    }

    @Transactional
    public void addRole(SysRole role) {
        if (sysRoleMapper.exists(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getCode, role.getCode()))) {
            throw new RuntimeException("角色编码已存在");
        }
        
        if (!role.getCode().startsWith("ROLE_")) {
            role.setCode("ROLE_" + role.getCode());
        }
        
        role.setCode(role.getCode().toUpperCase());
        sysRoleMapper.insert(role);
    }

    @Transactional
    public void updateRole(SysRole role) {
        SysRole existing = sysRoleMapper.selectById(role.getId());
        if (existing == null) {
            throw new RuntimeException("角色不存在");
        }
        
        if (SUPER_ADMIN_ROLE.equals(existing.getCode())) {
            throw new RuntimeException("超级管理员角色不可修改");
        }
        
        role.setCode(null);
        sysRoleMapper.updateById(role);
    }

    @Transactional
    public void deleteRole(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        
        if (SUPER_ADMIN_ROLE.equals(role.getCode())) {
            throw new RuntimeException("超级管理员角色不可删除");
        }
        
        int userCount = sysRoleMapper.selectUserCountByRoleId(id);
        if (userCount > 0) {
            throw new RuntimeException("该角色下还有" + userCount + "个用户，请先解除关联");
        }
        
        sysRoleMapper.deleteRoleMenusByRoleId(id);
        sysRoleMapper.deleteUserRolesByRoleId(id);
        sysRoleMapper.deleteById(id);
    }

    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return sysRoleMapper.selectMenuIdsByRoleId(roleId);
    }

    @Transactional
    public void assignMenus(Long roleId, Long[] menuIds) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        
        sysRoleMapper.deleteRoleMenusByRoleId(roleId);
        
        if (menuIds != null && menuIds.length > 0) {
            sysRoleMapper.insertRoleMenus(roleId, Arrays.asList(menuIds));
        }
    }
}

package com.studyagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyagent.entity.SysUser;
import com.studyagent.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserMapper sysUserMapper;
    private final AuthService authService;

    public Page<SysUser> getUserPage(Integer current, Integer size, String username, 
                                       Integer status, Long roleId, String startTime, String endTime) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(username)) {
            wrapper.like(SysUser::getUsername, username);
        }
        
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        
        if (StringUtils.hasText(startTime)) {
            wrapper.ge(SysUser::getCreateTime, startTime);
        }
        
        if (StringUtils.hasText(endTime)) {
            wrapper.le(SysUser::getCreateTime, endTime);
        }
        
        wrapper.orderByDesc(SysUser::getCreateTime);
        
        Page<SysUser> page = sysUserMapper.selectPage(new Page<>(current, size), wrapper);
        
        for (SysUser user : page.getRecords()) {
            List<String> roleNames = sysUserMapper.selectRoleNamesByUserId(user.getId());
            user.setRoleNames(String.join(",", roleNames));
        }
        
        return page;
    }

    public SysUser getById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user != null) {
            List<Long> roleIds = sysUserMapper.selectRoleIdsByUserId(id);
            user.setRoleIds(roleIds.toArray(new Long[0]));
        }
        return user;
    }

    @Transactional
    public void addUser(SysUser user) {
        if (sysUserMapper.exists(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, user.getUsername()))) {
            throw new RuntimeException("用户名已存在");
        }
        
        user.setPassword(authService.encryptPassword(user.getPassword()));
        user.setStatus(1);
        sysUserMapper.insert(user);
        
        if (user.getRoleIds() != null && user.getRoleIds().length > 0) {
            sysUserMapper.insertUserRoles(user.getId(), Arrays.asList(user.getRoleIds()));
        }
    }

    @Transactional
    public void updateUser(SysUser user) {
        SysUser existing = sysUserMapper.selectById(user.getId());
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setPassword(null);
        user.setUsername(null);
        sysUserMapper.updateById(user);
        
        if (user.getRoleIds() != null) {
            sysUserMapper.deleteUserRolesByUserId(user.getId());
            if (user.getRoleIds().length > 0) {
                sysUserMapper.insertUserRoles(user.getId(), Arrays.asList(user.getRoleIds()));
            }
        }
    }

    @Transactional
    public void deleteUser(Long id, Long currentUserId) {
        if (id.equals(currentUserId)) {
            throw new RuntimeException("不能删除当前登录账号");
        }
        
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (isSuperAdmin(id)) {
            throw new RuntimeException("不能删除超级管理员");
        }
        
        sysUserMapper.deleteUserRolesByUserId(id);
        sysUserMapper.deleteById(id);
    }

    @Transactional
    public void batchDelete(List<Long> ids, Long currentUserId) {
        for (Long id : ids) {
            deleteUser(id, currentUserId);
        }
    }

    @Transactional
    public void assignRoles(Long userId, Long[] roleIds) {
        if (roleIds == null || roleIds.length == 0) {
            throw new RuntimeException("至少需要分配一个角色");
        }
        
        sysUserMapper.deleteUserRolesByUserId(userId);
        sysUserMapper.insertUserRoles(userId, Arrays.asList(roleIds));
    }

    @Transactional
    public void resetPassword(Long id) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(authService.encryptPassword("Admin@123456"));
        sysUserMapper.updateById(user);
    }

    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (!authService.encryptPassword(oldPassword).equals(user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        user.setPassword(authService.encryptPassword(newPassword));
        sysUserMapper.updateById(user);
    }

    public void updateLoginInfo(Long userId, String loginIp) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setLoginIp(loginIp);
        user.setLoginDate(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    private boolean isSuperAdmin(Long userId) {
        return authService.isSuperAdmin(userId);
    }

    public List<SysUser> list() {
        return sysUserMapper.selectList(null);
    }
}

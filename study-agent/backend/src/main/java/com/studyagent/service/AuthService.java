package com.studyagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyagent.entity.SysMenu;
import com.studyagent.entity.SysRole;
import com.studyagent.entity.SysUser;
import com.studyagent.mapper.SysMenuMapper;
import com.studyagent.mapper.SysRoleMapper;
import com.studyagent.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String SALT = "StudyAgent_Secret_Key_2024";
    private static final String SUPER_ADMIN_ROLE = "ROLE_SUPER_ADMIN";

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper sysMenuMapper;

    public SysUser login(String username, String password) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
        );
        
        if (user == null) {
            return null;
        }
        
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }
        
        if (!encryptPassword(password).equals(user.getPassword())) {
            return null;
        }
        
        return user;
    }

    public List<SysRole> getUserRoles(Long userId) {
        return sysRoleMapper.selectRolesByUserId(userId);
    }

    public List<SysMenu> getUserMenus(Long userId) {
        List<SysRole> roles = getUserRoles(userId);
        
        boolean isSuperAdmin = roles.stream()
                .anyMatch(r -> SUPER_ADMIN_ROLE.equals(r.getCode()));
        
        if (isSuperAdmin) {
            return sysMenuMapper.selectList(
                    new LambdaQueryWrapper<SysMenu>()
                            .eq(SysMenu::getDel, 0)
                            .eq(SysMenu::getIsEnable, 1)
                            .orderByAsc(SysMenu::getParentId, SysMenu::getSort)
            );
        }
        
        return sysMenuMapper.selectMenusByUserId(userId);
    }

    public List<String> getUserPermissions(Long userId) {
        List<SysMenu> menus = getUserMenus(userId);
        return menus.stream()
                .filter(m -> m.getPermission() != null && !m.getPermission().isEmpty())
                .map(SysMenu::getPermission)
                .distinct()
                .collect(Collectors.toList());
    }

    public String encryptPassword(String password) {
        String salted = password + SALT;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(salted.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    public boolean isSuperAdmin(Long userId) {
        List<SysRole> roles = getUserRoles(userId);
        return roles.stream()
                .anyMatch(r -> SUPER_ADMIN_ROLE.equals(r.getCode()));
    }
}

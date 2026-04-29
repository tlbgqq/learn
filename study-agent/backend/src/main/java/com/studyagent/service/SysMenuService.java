package com.studyagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyagent.entity.SysMenu;
import com.studyagent.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMenuService {

    private final SysMenuMapper sysMenuMapper;

    public List<SysMenu> getMenuTree() {
        List<SysMenu> allMenus = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getDel, 0)
                        .orderByAsc(SysMenu::getSort)
        );
        
        return buildTree(allMenus, 0L);
    }

    public List<SysMenu> getUserMenuTree(Long userId) {
        List<SysMenu> userMenus = sysMenuMapper.selectMenusByUserId(userId);
        
        List<SysMenu> menuList = userMenus.stream()
                .filter(m -> m.getType() != 3)
                .collect(Collectors.toList());
        
        return buildTree(menuList, 0L);
    }

    private List<SysMenu> buildTree(List<SysMenu> menus, Long parentId) {
        return menus.stream()
                .filter(m -> m.getParentId().equals(parentId))
                .peek(m -> m.setChildren(buildTree(menus, m.getId())))
                .sorted(Comparator.comparingInt(SysMenu::getSort))
                .collect(Collectors.toList());
    }

    public SysMenu getById(Long id) {
        return sysMenuMapper.selectById(id);
    }

    @Transactional
    public void addMenu(SysMenu menu) {
        if (sysMenuMapper.exists(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, menu.getParentId())
                .eq(SysMenu::getName, menu.getName())
                .eq(SysMenu::getDel, 0))) {
            throw new RuntimeException("同级菜单名称已存在");
        }
        
        if (menu.getType() != 3 && StringUtils.hasText(menu.getPath())) {
            if (sysMenuMapper.exists(new LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getPath, menu.getPath())
                    .eq(SysMenu::getDel, 0))) {
                throw new RuntimeException("路由路径已存在");
            }
        }
        
        if (menu.getIsShow() == null) {
            menu.setIsShow(1);
        }
        if (menu.getIsEnable() == null) {
            menu.setIsEnable(1);
        }
        
        sysMenuMapper.insert(menu);
    }

    @Transactional
    public void updateMenu(SysMenu menu) {
        SysMenu existing = sysMenuMapper.selectById(menu.getId());
        if (existing == null) {
            throw new RuntimeException("菜单不存在");
        }
        
        if (!existing.getName().equals(menu.getName())) {
            if (sysMenuMapper.exists(new LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getParentId, menu.getParentId())
                    .eq(SysMenu::getName, menu.getName())
                    .eq(SysMenu::getDel, 0)
                    .ne(SysMenu::getId, menu.getId()))) {
                throw new RuntimeException("同级菜单名称已存在");
            }
        }
        
        if (menu.getType() != 3 && StringUtils.hasText(menu.getPath())) {
            if (!existing.getPath().equals(menu.getPath())) {
                if (sysMenuMapper.exists(new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getPath, menu.getPath())
                        .eq(SysMenu::getDel, 0)
                        .ne(SysMenu::getId, menu.getId()))) {
                    throw new RuntimeException("路由路径已存在");
                }
            }
        }
        
        sysMenuMapper.updateById(menu);
    }

    @Transactional
    public void deleteMenu(Long id) {
        SysMenu menu = sysMenuMapper.selectById(id);
        if (menu == null) {
            throw new RuntimeException("菜单不存在");
        }
        
        List<Long> childrenIds = getAllChildrenIds(id);
        childrenIds.add(id);
        
        for (Long menuId : childrenIds) {
            int roleCount = sysMenuMapper.selectRoleMenuCountByMenuId(menuId);
            if (roleCount > 0) {
                sysMenuMapper.deleteRoleMenusByMenuId(menuId);
            }
        }
        
        for (Long menuId : childrenIds) {
            sysMenuMapper.deleteById(menuId);
        }
    }

    private List<Long> getAllChildrenIds(Long parentId) {
        List<Long> result = new ArrayList<>();
        List<Long> childrenIds = sysMenuMapper.selectChildrenIdsByParentId(parentId);
        
        for (Long childId : childrenIds) {
            result.add(childId);
            result.addAll(getAllChildrenIds(childId));
        }
        
        return result;
    }

    @Transactional
    public void updateSort(List<Map<String, Object>> menuList) {
        for (Map<String, Object> menu : menuList) {
            Long id = Long.valueOf(menu.get("id").toString());
            Integer sort = Integer.valueOf(menu.get("sort").toString());
            Long parentId = Long.valueOf(menu.get("parentId").toString());
            
            SysMenu sysMenu = new SysMenu();
            sysMenu.setId(id);
            sysMenu.setSort(sort);
            sysMenu.setParentId(parentId);
            sysMenuMapper.updateById(sysMenu);
        }
    }

    public List<String> getUserPermissions(Long userId) {
        List<SysMenu> menus = sysMenuMapper.selectMenusByUserId(userId);
        return menus.stream()
                .filter(m -> m.getPermission() != null && !m.getPermission().isEmpty())
                .map(SysMenu::getPermission)
                .distinct()
                .collect(Collectors.toList());
    }
}

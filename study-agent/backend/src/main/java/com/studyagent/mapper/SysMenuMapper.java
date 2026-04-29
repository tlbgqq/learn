package com.studyagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyagent.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    
    List<SysMenu> selectMenusByRoleId(@Param("roleId") Long roleId);
    
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);
    
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
    
    List<Long> selectChildrenIdsByParentId(@Param("parentId") Long parentId);
    
    int selectRoleMenuCountByMenuId(@Param("menuId") Long menuId);
    
    void deleteRoleMenusByMenuId(@Param("menuId") Long menuId);
}

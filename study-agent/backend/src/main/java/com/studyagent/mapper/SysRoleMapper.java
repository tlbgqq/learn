package com.studyagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyagent.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);
    
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
    
    int selectUserCountByRoleId(@Param("roleId") Long roleId);
    
    void insertRoleMenus(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);
    
    void deleteRoleMenusByRoleId(@Param("roleId") Long roleId);
    
    void deleteUserRolesByRoleId(@Param("roleId") Long roleId);
}

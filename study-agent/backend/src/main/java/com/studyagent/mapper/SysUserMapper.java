package com.studyagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyagent.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
    
    List<String> selectRoleNamesByUserId(@Param("userId") Long userId);
    
    void insertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
    
    void deleteUserRolesByUserId(@Param("userId") Long userId);
}

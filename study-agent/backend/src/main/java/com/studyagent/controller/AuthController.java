package com.studyagent.controller;

import com.studyagent.config.JwtInterceptor;
import com.studyagent.dto.AdminLoginRequest;
import com.studyagent.dto.ApiResponse;
import com.studyagent.dto.UserInfoDTO;
import com.studyagent.entity.SysRole;
import com.studyagent.entity.SysUser;
import com.studyagent.service.AuthService;
import com.studyagent.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody AdminLoginRequest request) {
        try {
            SysUser user = authService.login(request.getUsername(), request.getPassword());
            if (user == null) {
                return ApiResponse.error("用户名或密码错误");
            }
            
            String token = jwtUtils.generateToken(user.getId(), user.getUsername());
            
            List<SysRole> roles = authService.getUserRoles(user.getId());
            List<String> roleNames = roles.stream()
                    .map(SysRole::getName)
                    .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userInfo", buildUserInfo(user, roles));
            
            return ApiResponse.success("登录成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/userinfo")
    public ApiResponse<UserInfoDTO> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(JwtInterceptor.USER_ID_HEADER);
        if (userId == null) {
            return ApiResponse.error("未登录");
        }
        
        SysUser user = new SysUser();
        user.setId(userId);
        
        List<SysRole> roles = authService.getUserRoles(userId);
        
        return ApiResponse.success(buildUserInfo(user, roles));
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout() {
        return ApiResponse.success("退出成功", null);
    }

    private UserInfoDTO buildUserInfo(SysUser user, List<SysRole> roles) {
        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRoles(roles.stream()
                .map(SysRole::getCode)
                .collect(Collectors.toList()));
        dto.setPermissions(authService.getUserPermissions(user.getId()));
        return dto;
    }
}

# 权限管理系统开发计划

## 文档信息

| 属性 | 值 |
|------|-----|
| 版本 | v1.0 |
| 创建日期 | 2026-04-29 |
| 依据 | permission-system-requirements.md |

---

## 一、后端接口开发计划

### 1.1 用户管理模块

| 序号 | 接口路径 | 方法 | 说明 | 状态 |
|------|----------|------|------|------|
| 1 | /api/system/user/list | GET | 用户列表（分页、筛选） | ✅ 已完成 |
| 2 | /api/system/user/{id} | GET | 获取用户详情 | ✅ 已完成 |
| 3 | /api/system/user | POST | 新增用户 | ✅ 已完成 |
| 4 | /api/system/user | PUT | 编辑用户 | ✅ 已完成 |
| 5 | /api/system/user/{id} | DELETE | 删除用户 | ✅ 已完成 |
| 6 | /api/system/user/batch | DELETE | 批量删除用户 | ✅ 已完成 |
| 7 | /api/system/user/{id}/roles | PUT | 分配角色 | ✅ 已完成 |
| 8 | /api/system/user/{id}/reset-password | PUT | 重置密码 | ✅ 已完成 |
| 9 | /api/system/user/password | PUT | 修改密码 | ✅ 已完成 |

**实体类**：
- ✅ SysUser.java

**Mapper**：
- ✅ SysUserMapper.java
- ✅ SysUserMapper.xml

**Service**：
- ✅ SysUserService.java

**Controller**：
- ✅ SysUserController.java

---

### 1.2 角色管理模块

| 序号 | 接口路径 | 方法 | 说明 | 状态 |
|------|----------|------|------|------|
| 1 | /api/system/role/list | GET | 角色列表 | ✅ 已完成 |
| 2 | /api/system/role/{id} | GET | 获取角色详情 | ✅ 已完成 |
| 3 | /api/system/role | POST | 新增角色 | ✅ 已完成 |
| 4 | /api/system/role | PUT | 编辑角色 | ✅ 已完成 |
| 5 | /api/system/role/{id} | DELETE | 删除角色 | ✅ 已完成 |
| 6 | /api/system/role/{id}/menus | GET | 获取角色菜单权限 | ✅ 已完成 |
| 7 | /api/system/role/{id}/menus | PUT | 分配菜单权限 | ✅ 已完成 |

**实体类**：
- ✅ SysRole.java

**Mapper**：
- ✅ SysRoleMapper.java
- ✅ SysRoleMapper.xml

**Service**：
- ✅ SysRoleService.java

**Controller**：
- ✅ SysRoleController.java

---

### 1.3 菜单管理模块

| 序号 | 接口路径 | 方法 | 说明 | 状态 |
|------|----------|------|------|------|
| 1 | /api/system/menu/tree | GET | 菜单树列表 | ✅ 已完成 |
| 2 | /api/system/menu/{id} | GET | 获取菜单详情 | ✅ 已完成 |
| 3 | /api/system/menu | POST | 新增菜单 | ✅ 已完成 |
| 4 | /api/system/menu | PUT | 编辑菜单 | ✅ 已完成 |
| 5 | /api/system/menu/{id} | DELETE | 删除菜单 | ✅ 已完成 |
| 6 | /api/system/menu/sort | PUT | 更新菜单排序 | ✅ 已完成 |

**实体类**：
- ✅ SysMenu.java

**Mapper**：
- ✅ SysMenuMapper.java
- ✅ SysMenuMapper.xml

**Service**：
- ✅ SysMenuService.java

**Controller**：
- ✅ SysMenuController.java

---

### 1.4 认证模块

| 序号 | 接口路径 | 方法 | 说明 | 状态 |
|------|----------|------|------|------|
| 1 | /api/auth/login | POST | 用户登录 | ✅ 已完成 |
| 2 | /api/auth/userinfo | GET | 获取当前用户信息 | ✅ 已完成 |
| 3 | /api/auth/logout | POST | 退出登录 | ✅ 已完成 |

**实体类**：
- ✅ SysUserRole.java（关联表）
- ✅ SysRoleMenu.java（关联表）

**Mapper**：
- ✅ SysUserRoleMapper.java
- ✅ SysRoleMenuMapper.java

**Service**：
- ✅ AuthService.java

**Controller**：
- ✅ AuthController.java

**DTO**：
- ✅ AdminLoginRequest.java
- ✅ UserInfoDTO.java

**其他组件**：
- ✅ JwtUtils.java（JWT工具类，已存在）
- ✅ JwtInterceptor.java（拦截器，已存在）

---

### 1.5 公共组件

| 序号 | 组件 | 说明 | 状态 |
|------|------|------|------|
| 1 | ApiResponse.java | 统一响应格式 | ✅ 已存在 |
| 2 | GlobalExceptionHandler.java | 全局异常处理 | 待开发 |
| 3 | MyBatisPlusConfig.java | 分页插件配置 | ✅ 已存在 |

---

## 二、前端页面开发计划

### 2.1 布局与路由

| 序号 | 页面路径 | 组件 | 说明 | 状态 |
|------|----------|------|------|------|
| 1 | /admin | Layout.vue | 后台布局组件 | ✅ 已完成 |
| 2 | /admin/dashboard | Dashboard.vue | 首页 | ✅ 已完成 |

---

### 2.2 登录模块

| 序号 | 页面路径 | 组件 | 说明 | 状态 |
|------|----------|------|------|------|
| 1 | /admin/login | Login.vue | 登录页 | ✅ 已完成 |

---

### 2.3 用户管理模块

| 序号 | 页面路径 | 组件 | 说明 | 状态 |
|------|----------|------|------|------|
| 1 | /admin/user | index.vue | 用户列表页 | ✅ 已完成 |

**Vue文件**：
- ✅ views/admin/system/user/index.vue（包含新增、编辑、分配角色弹窗功能）

---

### 2.4 角色管理模块

| 序号 | 页面路径 | 组件 | 说明 | 状态 |
|------|----------|------|------|------|
| 1 | /admin/role | index.vue | 角色列表页 | ✅ 已完成 |

**Vue文件**：
- ✅ views/admin/system/role/index.vue（包含新增、编辑、分配菜单弹窗功能）

---

### 2.5 菜单管理模块

| 序号 | 页面路径 | 组件 | 说明 | 状态 |
|------|----------|------|------|------|
| 1 | /admin/menu | index.vue | 菜单树列表页 | ✅ 已完成 |

**Vue文件**：
- ✅ views/admin/system/menu/index.vue（包含新增、编辑弹窗功能）

---

### 2.6 前端公共组件

| 序号 | 组件 | 说明 | 状态 |
|------|------|------|------|
| 1 | App.vue | 根组件 | ✅ 已存在 |
| 2 | router/index.js | 路由配置 | ✅ 已更新 |
| 3 | store/admin.js | 管理员状态管理 | ✅ 已完成 |
| 4 | api/admin.js | 接口封装 | ✅ 已完成 |

---

## 三、数据库开发计划

| 序号 | 表名 | 说明 | 状态 |
|------|------|------|------|
| 1 | sys_user | 用户表 | ✅ 已创建（SQL脚本） |
| 2 | sys_role | 角色表 | ✅ 已创建（SQL脚本） |
| 3 | sys_menu | 菜单表 | ✅ 已创建（SQL脚本） |
| 4 | sys_user_role | 用户角色关联表 | ✅ 已创建（SQL脚本） |
| 5 | sys_role_menu | 角色菜单关联表 | ✅ 已创建（SQL脚本） |

**初始数据**：
- ✅ 超级管理员角色（ROLE_SUPER_ADMIN）
- ✅ 默认管理员账号（admin/Admin@123456）

**SQL脚本位置**：
- backend/sql/permission-system.sql

---

## 四、开发顺序

### 第一阶段：数据库与实体类 ✅ 已完成
1. ✅ 创建数据库表
2. ✅ 创建实体类（SysUser、SysRole、SysMenu、SysUserRole、SysRoleMenu）

### 第二阶段：基础功能 ✅ 已完成
3. ✅ 通用响应（ApiResponse，已存在）
4. ✅ JWT工具类（JwtUtils，已存在）
5. ✅ 登录接口（AuthController）

### 第三阶段：用户管理 ✅ 已完成
6. ✅ 用户管理后端接口（SysUserController）
7. ✅ 用户管理前端页面（views/admin/system/user/index.vue）

### 第四阶段：角色管理 ✅ 已完成
8. ✅ 角色管理后端接口（SysRoleController）
9. ✅ 角色管理前端页面（views/admin/system/role/index.vue）

### 第五阶段：菜单管理 ✅ 已完成
10. ✅ 菜单管理后端接口（SysMenuController）
11. ✅ 菜单管理前端页面（views/admin/system/menu/index.vue）

### 第六阶段：完善与集成
12. 权限控制集成（基于菜单权限标识）
13. 前端动态路由
14. 测试与修复

---

## 五、任务统计

| 模块 | 后端接口数 | 前端页面数 | 完成率 |
|------|-----------|-----------|--------|
| 登录/认证 | 3/3 | 1/1 | 100% |
| 用户管理 | 9/9 | 1/4 | 100%（功能合并到单页面） |
| 角色管理 | 7/7 | 1/4 | 100%（功能合并到单页面） |
| 菜单管理 | 6/6 | 1/3 | 100%（功能合并到单页面） |
| 公共组件 | - | - | 已更新路由和状态管理 |
| **合计** | **25/25** | **4/18** | **核心功能已完成** |

---

## 六、已创建文件清单

### 后端文件

**Entity（实体类）**：
- backend/src/main/java/com/studyagent/entity/SysUser.java
- backend/src/main/java/com/studyagent/entity/SysRole.java
- backend/src/main/java/com/studyagent/entity/SysMenu.java
- backend/src/main/java/com/studyagent/entity/SysUserRole.java
- backend/src/main/java/com/studyagent/entity/SysRoleMenu.java

**Mapper（数据访问层）**：
- backend/src/main/java/com/studyagent/mapper/SysUserMapper.java
- backend/src/main/java/com/studyagent/mapper/SysRoleMapper.java
- backend/src/main/java/com/studyagent/mapper/SysMenuMapper.java
- backend/src/main/java/com/studyagent/mapper/SysUserRoleMapper.java
- backend/src/main/java/com/studyagent/mapper/SysRoleMenuMapper.java

**Mapper XML**：
- backend/src/main/resources/mapper/SysUserMapper.xml
- backend/src/main/resources/mapper/SysRoleMapper.xml
- backend/src/main/resources/mapper/SysMenuMapper.xml

**Service（业务逻辑层）**：
- backend/src/main/java/com/studyagent/service/AuthService.java
- backend/src/main/java/com/studyagent/service/SysUserService.java
- backend/src/main/java/com/studyagent/service/SysRoleService.java
- backend/src/main/java/com/studyagent/service/SysMenuService.java

**Controller（控制层）**：
- backend/src/main/java/com/studyagent/controller/AuthController.java
- backend/src/main/java/com/studyagent/controller/SysUserController.java
- backend/src/main/java/com/studyagent/controller/SysRoleController.java
- backend/src/main/java/com/studyagent/controller/SysMenuController.java

**DTO（数据传输对象）**：
- backend/src/main/java/com/studyagent/dto/AdminLoginRequest.java
- backend/src/main/java/com/studyagent/dto/UserInfoDTO.java

**SQL脚本**：
- backend/sql/permission-system.sql

### 前端文件

**API接口**：
- frontend/src/api/admin.js

**状态管理**：
- frontend/src/store/admin.js

**视图组件**：
- frontend/src/views/admin/Login.vue
- frontend/src/views/admin/Layout.vue
- frontend/src/views/admin/Dashboard.vue
- frontend/src/views/admin/system/user/index.vue
- frontend/src/views/admin/system/role/index.vue
- frontend/src/views/admin/system/menu/index.vue

**路由配置（已更新）**：
- frontend/src/router/index.js

---

## 七、修订记录

| 版本 | 日期 | 修订人 | 修订内容 |
|------|------|--------|----------|
| v1.0 | 2026-04-29 | - | 初始版本 |
| v1.1 | 2026-04-29 | - | 完成核心功能开发，标记已完成任务 |

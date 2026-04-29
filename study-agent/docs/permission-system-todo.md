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
| 1 | /api/system/user/list | GET | 用户列表（分页、筛选） | 待开发 |
| 2 | /api/system/user/{id} | GET | 获取用户详情 | 待开发 |
| 3 | /api/system/user | POST | 新增用户 | 待开发 |
| 4 | /api/system/user | PUT | 编辑用户 | 待开发 |
| 5 | /api/system/user/{id} | DELETE | 删除用户 | 待开发 |
| 6 | /api/system/user/batch | DELETE | 批量删除用户 | 待开发 |
| 7 | /api/system/user/{id}/roles | PUT | 分配角色 | 待开发 |
| 8 | /api/system/user/{id}/reset-password | PUT | 重置密码 | 待开发 |
| 9 | /api/system/user/password | PUT | 修改密码 | 待开发 |

**实体类**：
- SysUser.java

**Mapper**：
- SysUserMapper.java

**Service**：
- SysUserService.java
- SysUserServiceImpl.java

**Controller**：
- SysUserController.java

---

### 1.2 角色管理模块

| 序号 | 接口路径 | 方法 | 说明 | 状态 |
|------|----------|------|------|------|
| 1 | /api/system/role/list | GET | 角色列表 | 待开发 |
| 2 | /api/system/role/{id} | GET | 获取角色详情 | 待开发 |
| 3 | /api/system/role | POST | 新增角色 | 待开发 |
| 4 | /api/system/role | PUT | 编辑角色 | 待开发 |
| 5 | /api/system/role/{id} | DELETE | 删除角色 | 待开发 |
| 6 | /api/system/role/{id}/menus | GET | 获取角色菜单权限 | 待开发 |
| 7 | /api/system/role/{id}/menus | PUT | 分配菜单权限 | 待开发 |

**实体类**：
- SysRole.java

**Mapper**：
- SysRoleMapper.java

**Service**：
- SysRoleService.java
- SysRoleServiceImpl.java

**Controller**：
- SysRoleController.java

---

### 1.3 菜单管理模块

| 序号 | 接口路径 | 方法 | 说明 | 状态 |
|------|----------|------|------|------|
| 1 | /api/system/menu/tree | GET | 菜单树列表 | 待开发 |
| 2 | /api/system/menu/{id} | GET | 获取菜单详情 | 待开发 |
| 3 | /api/system/menu | POST | 新增菜单 | 待开发 |
| 4 | /api/system/menu | PUT | 编辑菜单 | 待开发 |
| 5 | /api/system/menu/{id} | DELETE | 删除菜单 | 待开发 |
| 6 | /api/system/menu/sort | PUT | 更新菜单排序 | 待开发 |

**实体类**：
- SysMenu.java

**Mapper**：
- SysMenuMapper.java

**Service**：
- SysMenuService.java
- SysMenuServiceImpl.java

**Controller**：
- SysMenuController.java

---

### 1.4 认证模块

| 序号 | 接口路径 | 方法 | 说明 | 状态 |
|------|----------|------|------|------|
| 1 | /api/auth/login | POST | 用户登录 | 待开发 |
| 2 | /api/auth/userinfo | GET | 获取当前用户信息 | 待开发 |
| 3 | /api/auth/logout | POST | 退出登录 | 待开发 |

**实体类**：
- SysUserRole.java（关联表）
- SysRoleMenu.java（关联表）

**Mapper**：
- SysUserRoleMapper.java
- SysRoleMenuMapper.java

**Service**：
- AuthService.java
- AuthServiceImpl.java

**Controller**：
- AuthController.java

**其他组件**：
- JwtUtil.java（JWT工具类）
- JwtInterceptor.java（拦截器）
- SecurityConfig.java（安全配置）

---

### 1.5 公共组件

| 序号 | 组件 | 说明 | 状态 |
|------|------|------|------|
| 1 | ApiResponse.java | 统一响应格式 | 待开发 |
| 2 | GlobalExceptionHandler.java | 全局异常处理 | 待开发 |
| 3 | MyBatisPlusConfig.java | 分页插件配置 | 待开发 |

---

## 二、前端页面开发计划

### 2.1 布局与路由

| 序号 | 页面路径 | 组件 | 说明 | 状态 |
|------|----------|------|------|------|
| 1 | /layout | Layout.vue | 后台布局组件 | 待开发 |
| 2 | / | index.vue | 首页 | 待开发 |

---

### 2.2 登录模块

| 序号 | 页面路径 | 组件 | 说明 | 状态 |
|------|----------|------|------|------|
| 1 | /login | Login.vue | 登录页 | 待开发 |

---

### 2.3 用户管理模块

| 序号 | 页面路径 | 组件 | 说明 | 状态 |
|------|----------|------|------|------|
| 1 | /system/user | UserList.vue | 用户列表页 | 待开发 |
| 2 | /system/user/add | UserAdd.vue | 新增用户弹窗 | 待开发 |
| 3 | /system/user/edit | UserEdit.vue | 编辑用户弹窗 | 待开发 |
| 4 | /system/user/assign-role | AssignRole.vue | 分配角色弹窗 | 待开发 |

**Vue文件**：
- views/system/user/index.vue
- views/system/user/components/UserDialog.vue
- views/system/user/components/AssignRoleDialog.vue

---

### 2.4 角色管理模块

| 序号 | 页面路径 | 组件 | 说明 | 状态 |
|------|----------|------|------|------|
| 1 | /system/role | RoleList.vue | 角色列表页 | 待开发 |
| 2 | /system/role/add | RoleAdd.vue | 新增角色弹窗 | 待开发 |
| 3 | /system/role/edit | RoleEdit.vue | 编辑角色弹窗 | 待开发 |
| 4 | /system/role/assign-menu | AssignMenu.vue | 分配菜单权限弹窗 | 待开发 |

**Vue文件**：
- views/system/role/index.vue
- views/system/role/components/RoleDialog.vue
- views/system/role/components/AssignMenuDialog.vue

---

### 2.5 菜单管理模块

| 序号 | 页面路径 | 组件 | 说明 | 状态 |
|------|----------|------|------|------|
| 1 | /system/menu | MenuTree.vue | 菜单树列表页 | 待开发 |
| 2 | /system/menu/add | MenuAdd.vue | 新增菜单弹窗 | 待开发 |
| 3 | /system/menu/edit | MenuEdit.vue | 编辑菜单弹窗 | 待开发 |

**Vue文件**：
- views/system/menu/index.vue
- views/system/menu/components/MenuDialog.vue

---

### 2.6 前端公共组件

| 序号 | 组件 | 说明 | 状态 |
|------|------|------|------|
| 1 | App.vue | 根组件 | 待开发 |
| 2 | router/index.js | 路由配置 | 待开发 |
| 3 | store/index.js | 状态管理 | 待开发 |
| 4 | api/system/*.js | 接口封装 | 待开发 |
| 5 | utils/auth.js | 认证工具 | 待开发 |
| 6 | utils/permission.js | 权限指令 | 待开发 |

---

## 三、数据库开发计划

| 序号 | 表名 | 说明 | 状态 |
|------|------|------|------|
| 1 | sys_user | 用户表 | 待创建 |
| 2 | sys_role | 角色表 | 待创建 |
| 3 | sys_menu | 菜单表 | 待创建 |
| 4 | sys_user_role | 用户角色关联表 | 待创建 |
| 5 | sys_role_menu | 角色菜单关联表 | 待创建 |

**初始数据**：
- 超级管理员角色（ROLE_SUPER_ADMIN）
- 默认管理员账号（admin/Admin@123456）

---

## 四、开发顺序

### 第一阶段：数据库与实体类
1. 创建数据库表
2. 创建实体类（SysUser、SysRole、SysMenu、SysUserRole、SysRoleMenu）

### 第二阶段：基础功能
3. 通用响应（ApiResponse）
4. JWT工具类
5. 登录接口

### 第三阶段：用户管理
6. 用户管理后端接口
7. 用户管理前端页面

### 第四阶段：角色管理
8. 角色管理后端接口
9. 角色管理前端页面

### 第五阶段：菜单管理
10. 菜单管理后端接口
11. 菜单管理前端页面

### 第六阶段：完善与集成
12. 权限控制集成
13. 前端动态路由
14. 测试与修复

---

## 五、任务统计

| 模块 | 后端接口数 | 前端页面数 |
|------|-----------|-----------|
| 登录/认证 | 3 | 1 |
| 用户管理 | 9 | 4 |
| 角色管理 | 7 | 4 |
| 菜单管理 | 6 | 3 |
| 公共组件 | - | 6 |
| **合计** | **25** | **18** |

---

## 六、修订记录

| 版本 | 日期 | 修订人 | 修订内容 |
|------|------|--------|----------|
| v1.0 | 2026-04-29 | - | 初始版本 |

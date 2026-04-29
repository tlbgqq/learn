# 后台管理系统权限管理模块需求文档

## 文档信息

| 属性 | 值 |
|------|-----|
| 版本 | v1.0 |
| 创建日期 | 2026-04-29 |
| 技术栈 | Java Spring Boot + MyBatisPlus / Vue 3 + Vite / MySQL |

---

## 1. 项目概述

### 1.1 项目定位

后台管理系统（Admin Management System）是面向企业内部管理人员使用的 Web 应用平台，用于实现系统配置、用户管理、内容管理等核心功能的可视化操作。权限管理模块作为系统的基础架构组件，负责构建完整的身份认证与访问控制体系，确保系统操作的安全性与合规性。

### 1.2 设计目标

本模块设计遵循以下核心原则：

- **最小权限原则**：用户仅被授予完成工作所必需的最小权限集
- **职责分离原则**：通过角色机制实现权限的逻辑隔离
- **可审计原则**：所有权限变更操作均需记录日志，支持追溯
- **可扩展原则**：采用模块化设计，便于后续功能扩展

### 1.3 术语定义

| 术语 | 定义 |
|------|------|
| 用户（User） | 系统的实际使用者，拥有登录凭证和基本属性 |
| 角色（Role） | 权限的逻辑分组，代表一类职能岗位 |
| 菜单（Menu） | 系统功能入口，分为目录级、菜单级、按钮级 |
| 权限（Permission） | 用户或角色对特定资源的访问能力 |
| 用户角色关联 | 用户与角色之间的多对多关系 |
| 角色菜单关联 | 角色与菜单之间的多对多关系 |

---

## 2. 功能模块划分

### 2.1 模块结构图

```
权限管理系统
├── 用户管理（User Management）
│   ├── 用户列表
│   ├── 新增用户
│   ├── 编辑用户
│   ├── 删除用户
│   ├── 分配角色
│   └── 重置密码
├── 角色管理（Role Management）
│   ├── 角色列表
│   ├── 新增角色
│   ├── 编辑角色
│   ├── 删除角色
│   └── 分配菜单权限
└── 菜单管理（Menu Management）
    ├── 菜单树列表
    ├── 新增菜单
    ├── 编辑菜单
    ├── 删除菜单
    └── 拖拽排序
```

### 2.2 模块关系

三个模块之间存在如下关联关系：

```
用户管理 ←→ 角色管理 ←→ 菜单管理
   ↓            ↓            ↓
用户角色关联表 ←→ 角色菜单关联表
```

- 用户与角色：多对多关系
- 角色与菜单：多对多关系
- 用户与菜单：通过角色间接关联

---

## 3. 详细功能描述

### 3.1 用户管理模块

#### 3.1.1 用户列表

**功能描述**：展示系统中所有用户的信息，支持分页、筛选、搜索。

**字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| 序号 | Integer | 自动递增编号 |
| 用户名 | String | 登录账号，唯一 |
| 状态 | Enum | 启用/禁用 |
| 角色 | String | 关联的角色名称（多个以逗号分隔） |
| 创建时间 | DateTime | 账号创建时间 |
| 最后登录 | DateTime | 最近一次登录时间 |
| 操作 | Action | 编辑/删除/重置密码/分配角色 |

**筛选条件**：

- 按用户名关键字搜索
- 按状态筛选（全部/启用/禁用）
- 按角色筛选
- 按创建时间范围筛选

#### 3.1.2 新增用户

**必填字段**：

- 用户名（6-20位，字母开头，支持字母数字下划线）
- 初始密码（8-20位，须包含大小写字母和数字）
- 角色（至少选择一个）

**业务规则**：

- 用户名不允许重复
- 新建用户默认启用状态
- 新建用户默认无最后登录时间

#### 3.1.3 编辑用户

**可编辑字段**：

- 状态（启用/禁用）
- 角色分配

**业务规则**：

- 用户名不可修改
- 禁用用户禁止登录
- 角色变更即时生效

#### 3.1.4 删除用户

**删除类型**：

- 逻辑删除：将 del 字段置为 1（推荐）

**业务规则**：

- 删除前需确认，防止误操作
- 不允许删除当前登录账号
- 不允许删除超级管理员账号
- 删除后解除所有角色关联

#### 3.1.5 分配角色

**功能描述**：为用户分配或撤销角色。

**交互方式**：

- 左侧展示已有角色（已分配的打勾）
- 右侧展示可选角色列表
- 支持多选
- 提交后即时更新

**业务规则**：

- 至少保留一个角色
- 角色变更记录需写入操作日志

#### 3.1.6 重置密码

**功能描述**：将用户密码重置为初始密码。

**业务规则**：

- 重置后密码设为默认密码（如 `Admin@123456`）
- 重置后强制要求首次登录修改密码
- 记录重置操作人和时间

---

### 3.2 角色管理模块

#### 3.2.1 角色列表

**字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| 序号 | Integer | 自动递增编号 |
| 角色编码 | String | 唯一标识，如 `ROLE_ADMIN` |
| 角色名称 | String | 显示名称，如"系统管理员" |
| 排序号 | Integer | 列表展示顺序 |
| 状态 | Enum | 启用/禁用 |
| 描述 | String | 角色说明 |
| 用户数 | Integer | 关联的用户数量 |
| 创建时间 | DateTime | 创建时间 |
| 操作 | Action | 编辑/删除/分配权限 |

#### 3.2.2 新增角色

**必填字段**：

- 角色编码（大写下划线格式，如 `ROLE_TEACHER`）
- 角色名称
- 排序号

**可选字段**：

- 描述
- 状态（默认启用）

**业务规则**：

- 角色编码全局唯一，不允许重复
- 角色编码以 `ROLE_` 开头
- 系统内置角色（如 `ROLE_SUPER_ADMIN`）不可删除

#### 3.2.3 编辑角色

**可编辑字段**：

- 角色名称、描述、排序号、状态

**业务规则**：

- 角色编码不可修改
- 禁用角色后，所有关联用户失去该角色权限

#### 3.2.4 删除角色

**业务规则**：

- 删除前检查是否有用户关联
- 如有关联用户，提示先解除关联
- 系统内置角色不可删除
- 删除成功后级联删除角色菜单关联

#### 3.2.5 分配菜单权限

**功能描述**：为角色配置可访问的菜单和按钮。

**交互方式**：

- 以树形结构展示所有菜单
- 支持勾选菜单节点
- 勾选父级菜单时自动勾选所有子级
- 显示已选菜单数量统计

**业务规则**：

- 权限变更记录写入日志

---

### 3.3 菜单管理模块

#### 3.3.1 菜单树列表

**字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| 菜单名称 | String | 菜单显示名称 |
| 菜单图标 | String | 图标名称或路径 |
| 菜单类型 | Enum | 目录/菜单/按钮 |
| 路由路径 | String | 访问路径 |
| 排序号 | Integer | 同级菜单排序 |
| 状态 | Enum | 显示/隐藏/禁用 |
| 权限标识 | String | 按钮级别权限标识（仅按钮类型） |
| 创建时间 | DateTime | 创建时间 |
| 操作 | Action | 新增子级/编辑/删除 |

**菜单类型说明**：

| 类型 | 说明 | 示例 |
|------|------|------|
| 目录 | 左侧菜单的一级分类，不可跳转 | 系统管理 |
| 菜单 | 可点击跳转的叶子节点 | 用户列表 |
| 按钮 | 页面内的操作按钮 | 新增、编辑、删除 |

#### 3.3.2 新增菜单

**必填字段**：

- 菜单名称
- 菜单类型
- 上级菜单（顶级则选择"无"）

**根据菜单类型的额外字段**：

| 类型 | 额外必填字段 |
|------|-------------|
| 目录 | 菜单图标、排序号 |
| 菜单 | 路由路径、菜单图标、排序号 |
| 按钮 | 权限标识、所属菜单 |

**可选字段**：

- 路由参数
- 查询参数
- 是否缓存
- 是否显示

**业务规则**：

- 同一父级下菜单名称不允许重复
- 路由路径全局唯一
- 按钮级别菜单不需要设置路由

#### 3.3.3 编辑菜单

**业务规则**：

- 移动菜单（变更父级）时需更新所有子级的层级路径
- 目录不可直接变更为按钮类型

#### 3.3.4 删除菜单

**业务规则**：

- 删除父级菜单时需级联删除所有子级
- 删除前检查是否有角色关联该菜单
- 不允许删除系统内置菜单

#### 3.3.5 拖拽排序

**功能描述**：通过拖拽调整菜单的展示顺序和层级关系。

**业务规则**：

- 排序变更后更新所有相关菜单的 sort 字段
- 记录排序变更日志

---

## 4. 数据表设计

### 4.1 ER 图

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│      sys_user   │     │   sys_role_menu │     │     sys_menu    │
├─────────────────┤     ├─────────────────┤     ├─────────────────┤
│ id (PK)         │     │ id (PK)         │     │ id (PK)         │
│ username        │     │ role_id (FK)    │     │ name            │
│ password        │     │ menu_id (FK)    │     │ parent_id       │
│ status          │     └────────┬────────┘     │ type            │
│ del             │              │              │ path            │
│ create_time     │     ┌────────┴────────┐     │ icon            │
│ modify_time     │     │    sys_role     │     │ sort            │
└────────┬────────┘     ├─────────────────┤     │ permission      │
         │               │ id (PK)         │     │ status          │
         │               │ code            │     │ del             │
         │               │ name            │     │ create_time     │
         │               │ sort            │     │ modify_time     │
         │               │ status          │     └─────────────────┘
         │               │ description     │
         │               │ del             │
         │               │ create_time     │
         │               │ modify_time     │
         │               └────────┬────────┘
         │                        │
         │               ┌────────┴────────┐
         │               │  sys_user_role   │
         │               ├─────────────────┤
         │               │ id (PK)          │
         └──────────────►│ user_id (FK)     │
                         │ role_id (FK)     │
                         │ create_time      │
                         └─────────────────┘
```

### 4.2 用户表 (sys_user)

```sql
-- 用户表
CREATE TABLE sys_user (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username        VARCHAR(20)     NOT NULL COMMENT '用户名',
    password        VARCHAR(128)    NOT NULL COMMENT '密码（加密存储）',
    status          TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    login_ip        VARCHAR(50)     NOT NULL DEFAULT '' COMMENT '最后登录IP',
    login_date      DATETIME        DEFAULT NULL COMMENT '最后登录时间',
    del             TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '删除标记: 0-否, 1-是',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建索引的语句单独写
CREATE INDEX idx_sys_user_status ON sys_user(status);
CREATE INDEX idx_sys_user_create_time ON sys_user(create_time);
CREATE INDEX idx_sys_user_del ON sys_user(del);
```

### 4.3 角色表 (sys_role)

```sql
-- 角色表
CREATE TABLE sys_role (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    code            VARCHAR(50)     NOT NULL COMMENT '角色编码',
    name            VARCHAR(30)     NOT NULL COMMENT '角色名称',
    sort            INT             NOT NULL DEFAULT 0 COMMENT '排序号',
    status          TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    description     VARCHAR(200)    NOT NULL DEFAULT '' COMMENT '角色描述',
    del             TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '删除标记: 0-否, 1-是',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 创建索引的语句单独写
CREATE INDEX idx_sys_role_status ON sys_role(status);
CREATE INDEX idx_sys_role_del ON sys_role(del);
CREATE INDEX idx_sys_role_sort ON sys_role(sort);
```

### 4.4 菜单表 (sys_menu)

```sql
-- 菜单表
CREATE TABLE sys_menu (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    parent_id       BIGINT          NOT NULL DEFAULT 0 COMMENT '父级菜单ID: 0-顶级',
    name            VARCHAR(50)     NOT NULL COMMENT '菜单名称',
    type            TINYINT(1)      NOT NULL COMMENT '菜单类型: 1-目录, 2-菜单, 3-按钮',
    path            VARCHAR(200)    NOT NULL DEFAULT '' COMMENT '路由路径',
    icon            VARCHAR(50)     NOT NULL DEFAULT '' COMMENT '菜单图标',
    sort            INT             NOT NULL DEFAULT 0 COMMENT '排序号',
    permission      VARCHAR(100)    NOT NULL DEFAULT '' COMMENT '权限标识',
    is_show         TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '是否显示: 0-否, 1-是',
    is_enable       TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '是否启用: 0-否, 1-是',
    del             TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '删除标记: 0-否, 1-是',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- 创建索引的语句单独写
CREATE INDEX idx_sys_menu_parent_id ON sys_menu(parent_id);
CREATE INDEX idx_sys_menu_type ON sys_menu(type);
CREATE INDEX idx_sys_menu_sort ON sys_menu(sort);
CREATE INDEX idx_sys_menu_del ON sys_menu(del);
CREATE INDEX idx_sys_menu_path ON sys_menu(path);
CREATE INDEX idx_sys_menu_permission ON sys_menu(permission);
```

### 4.5 用户角色关联表 (sys_user_role)

```sql
-- 用户角色关联表
CREATE TABLE sys_user_role (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id         BIGINT          NOT NULL COMMENT '用户ID',
    role_id         BIGINT          NOT NULL COMMENT '角色ID',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 创建索引的语句单独写
CREATE INDEX idx_sys_user_role_user_id ON sys_user_role(user_id);
CREATE INDEX idx_sys_user_role_role_id ON sys_user_role(role_id);
```

### 4.6 角色菜单关联表 (sys_role_menu)

```sql
-- 角色菜单关联表
CREATE TABLE sys_role_menu (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id         BIGINT          NOT NULL COMMENT '角色ID',
    menu_id         BIGINT          NOT NULL COMMENT '菜单ID',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- 创建索引的语句单独写
CREATE INDEX idx_sys_role_menu_role_id ON sys_role_menu(role_id);
CREATE INDEX idx_sys_role_menu_menu_id ON sys_role_menu(menu_id);
```

---

## 5. API 接口设计

### 5.1 接口规范

#### 5.1.1 通用响应格式

```json
{
    "code": 200,
    "message": "操作成功",
    "data": { },
    "timestamp": 1714329600000
}
```

**响应码说明**：

| 响应码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或登录过期 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

#### 5.1.2 分页响应格式

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "records": [ ],
        "total": 100,
        "size": 10,
        "current": 1,
        "pages": 10
    },
    "timestamp": 1714329600000
}
```

#### 5.1.3 请求头规范

```
Content-Type: application/json
Authorization: Bearer {token}
```

---

### 5.2 用户管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/system/user/list | 用户列表 |
| GET | /api/system/user/{id} | 获取用户详情 |
| POST | /api/system/user | 新增用户 |
| PUT | /api/system/user | 编辑用户 |
| DELETE | /api/system/user/{id} | 删除用户 |
| DELETE | /api/system/user/batch | 批量删除用户 |
| PUT | /api/system/user/{id}/roles | 分配角色 |
| PUT | /api/system/user/{id}/reset-password | 重置密码 |
| PUT | /api/system/user/password | 修改密码 |

### 5.3 角色管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/system/role/list | 角色列表 |
| GET | /api/system/role/{id} | 获取角色详情 |
| POST | /api/system/role | 新增角色 |
| PUT | /api/system/role | 编辑角色 |
| DELETE | /api/system/role/{id} | 删除角色 |
| GET | /api/system/role/{id}/menus | 获取角色菜单权限 |
| PUT | /api/system/role/{id}/menus | 分配菜单权限 |

### 5.4 菜单管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/system/menu/tree | 菜单树列表 |
| GET | /api/system/menu/{id} | 获取菜单详情 |
| POST | /api/system/menu | 新增菜单 |
| PUT | /api/system/menu | 编辑菜单 |
| DELETE | /api/system/menu/{id} | 删除菜单 |
| PUT | /api/system/menu/sort | 更新菜单排序 |

### 5.5 登录认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 用户登录 |
| GET | /api/auth/userinfo | 获取当前用户信息 |
| POST | /api/auth/logout | 退出登录 |

---

## 6. 菜单树形结构设计

### 6.1 数据模型

菜单树采用**邻接表模型**（Adjacency List），通过 `parent_id` 字段建立父子关系：

- `parent_id = 0`：表示顶级菜单（根节点）
- `parent_id > 0`：表示有父级菜单

### 6.2 树形结构构建算法

#### 后端构建（Java）

```java
public List<MenuVO> buildTree(List<Menu> menus, Long parentId) {
    return menus.stream()
        .filter(menu -> menu.getParentId().equals(parentId))
        .peek(menu -> menu.setChildren(buildTree(menus, menu.getId())))
        .sorted(Comparator.comparingInt(Menu::getSort))
        .collect(Collectors.toList());
}
```

### 6.3 菜单层级限制

建议菜单层级控制在 **3 级以内**：

```
一级目录（系统管理）
  └── 二级菜单（用户管理）
        └── 三级按钮（新增/编辑/删除）
```

---

## 7. 权限控制设计方案

### 7.1 权限控制架构

```
┌─────────────────────────────────────────────────────────────┐
│                         前端 (Vue 3)                         │
├─────────────────────────────────────────────────────────────┤
│  路由守卫 (Navigation Guard)                                 │
│    ├── 登录状态检查                                           │
│    └── 菜单权限过滤                                           │
├─────────────────────────────────────────────────────────────┤
│                         后端 (Spring Boot)                   │
├─────────────────────────────────────────────────────────────┤
│  请求拦截器 (Interceptor)                                    │
│    ├── JWT Token 验证                                        │
│    └── 登录状态检查                                           │
├─────────────────────────────────────────────────────────────┤
│  权限注解 (@RequiresPermissions)                             │
│    └── 方法级别权限校验                                        │
└─────────────────────────────────────────────────────────────┘
```

### 7.2 权限模型

采用 **RBAC（基于角色的访问控制）** 模型：

```
用户 → 用户角色关联 → 角色 → 角色菜单关联 → 菜单/权限
```

**权限标识格式**：`{模块}:{资源}:{操作}`

| 权限标识 | 说明 |
|----------|------|
| system:user:list | 查看用户列表 |
| system:user:add | 新增用户 |
| system:user:edit | 编辑用户 |
| system:user:delete | 删除用户 |
| system:user:resetPwd | 重置密码 |
| system:user:assignRole | 分配角色 |

### 7.3 超级管理员

系统内置超级管理员角色，具有以下特性：

| 属性 | 值 |
|------|-----|
| 角色编码 | ROLE_SUPER_ADMIN |
| 角色名称 | 超级管理员 |
| 权限范围 | 全部权限 |
| 是否可删除 | 否 |
| 是否可修改 | 否 |

---

## 8. 附录

### 8.1 字典码说明

| 字典类型 | 字典值 | 说明 |
|----------|--------|------|
| user_sex | 0 | 未知 |
| user_sex | 1 | 男 |
| user_sex | 2 | 女 |
| user_status | 0 | 禁用 |
| user_status | 1 | 启用 |
| menu_type | 1 | 目录 |
| menu_type | 2 | 菜单 |
| menu_type | 3 | 按钮 |
| menu_show | 0 | 隐藏 |
| menu_show | 1 | 显示 |
| menu_enable | 0 | 禁用 |
| menu_enable | 1 | 启用 |
| menu_keep_alive | 0 | 不缓存 |
| menu_keep_alive | 1 | 缓存 |

### 8.2 默认数据

#### 默认管理员账号

| 字段 | 值 |
|------|-----|
| 用户名 | admin |
| 密码 | Admin@123456 |
| 角色 | 超级管理员 |

#### 默认角色

| 角色编码 | 角色名称 | 说明 |
|----------|----------|------|
| ROLE_SUPER_ADMIN | 超级管理员 | 系统最高权限 |
| ROLE_ADMIN | 系统管理员 | 系统管理权限 |
| ROLE_USER | 普通用户 | 基础访问权限 |

---

## 9. 修订记录

| 版本 | 日期 | 修订人 | 修订内容 |
|------|------|--------|----------|
| v1.0 | 2026-04-29 | - | 初始版本 |

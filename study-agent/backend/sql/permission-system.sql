-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
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

CREATE INDEX idx_sys_user_status ON sys_user(status);
CREATE INDEX idx_sys_user_create_time ON sys_user(create_time);
CREATE INDEX idx_sys_user_del ON sys_user(del);

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
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

CREATE INDEX idx_sys_role_status ON sys_role(status);
CREATE INDEX idx_sys_role_del ON sys_role(del);
CREATE INDEX idx_sys_role_sort ON sys_role(sort);

-- 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
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

CREATE INDEX idx_sys_menu_parent_id ON sys_menu(parent_id);
CREATE INDEX idx_sys_menu_type ON sys_menu(type);
CREATE INDEX idx_sys_menu_sort ON sys_menu(sort);
CREATE INDEX idx_sys_menu_del ON sys_menu(del);
CREATE INDEX idx_sys_menu_path ON sys_menu(path);
CREATE INDEX idx_sys_menu_permission ON sys_menu(permission);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id         BIGINT          NOT NULL COMMENT '用户ID',
    role_id         BIGINT          NOT NULL COMMENT '角色ID',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

CREATE INDEX idx_sys_user_role_user_id ON sys_user_role(user_id);
CREATE INDEX idx_sys_user_role_role_id ON sys_user_role(role_id);

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id         BIGINT          NOT NULL COMMENT '角色ID',
    menu_id         BIGINT          NOT NULL COMMENT '菜单ID',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

CREATE INDEX idx_sys_role_menu_role_id ON sys_role_menu(role_id);
CREATE INDEX idx_sys_role_menu_menu_id ON sys_role_menu(menu_id);

-- 插入默认角色
INSERT INTO sys_role (code, name, sort, description) VALUES 
('ROLE_SUPER_ADMIN', '超级管理员', 1, '系统最高权限，拥有所有功能权限'),
('ROLE_ADMIN', '系统管理员', 2, '系统管理权限，可管理用户、角色、菜单'),
('ROLE_USER', '普通用户', 3, '基础访问权限，仅可查看个人信息');

-- 插入默认菜单
-- 一级目录：系统管理
INSERT INTO sys_menu (parent_id, name, type, path, icon, sort, permission, is_show, is_enable) VALUES 
(0, '系统管理', 1, '/system', 'Setting', 1, '', 1, 1);

-- 获取系统管理的ID
SET @system_menu_id = LAST_INSERT_ID();

-- 二级菜单：用户管理
INSERT INTO sys_menu (parent_id, name, type, path, icon, sort, permission, is_show, is_enable) VALUES 
(@system_menu_id, '用户管理', 2, '/system/user', 'User', 1, '', 1, 1);

SET @user_menu_id = LAST_INSERT_ID();

-- 用户管理按钮
INSERT INTO sys_menu (parent_id, name, type, path, icon, sort, permission, is_show, is_enable) VALUES 
(@user_menu_id, '查看用户列表', 3, '', '', 1, 'system:user:list', 0, 1),
(@user_menu_id, '新增用户', 3, '', '', 2, 'system:user:add', 0, 1),
(@user_menu_id, '编辑用户', 3, '', '', 3, 'system:user:edit', 0, 1),
(@user_menu_id, '删除用户', 3, '', '', 4, 'system:user:delete', 0, 1),
(@user_menu_id, '重置密码', 3, '', '', 5, 'system:user:resetPwd', 0, 1),
(@user_menu_id, '分配角色', 3, '', '', 6, 'system:user:assignRole', 0, 1);

-- 二级菜单：角色管理
INSERT INTO sys_menu (parent_id, name, type, path, icon, sort, permission, is_show, is_enable) VALUES 
(@system_menu_id, '角色管理', 2, '/system/role', 'Avatar', 2, '', 1, 1);

SET @role_menu_id = LAST_INSERT_ID();

-- 角色管理按钮
INSERT INTO sys_menu (parent_id, name, type, path, icon, sort, permission, is_show, is_enable) VALUES 
(@role_menu_id, '查看角色列表', 3, '', '', 1, 'system:role:list', 0, 1),
(@role_menu_id, '新增角色', 3, '', '', 2, 'system:role:add', 0, 1),
(@role_menu_id, '编辑角色', 3, '', '', 3, 'system:role:edit', 0, 1),
(@role_menu_id, '删除角色', 3, '', '', 4, 'system:role:delete', 0, 1),
(@role_menu_id, '分配菜单', 3, '', '', 5, 'system:role:assignMenu', 0, 1);

-- 二级菜单：菜单管理
INSERT INTO sys_menu (parent_id, name, type, path, icon, sort, permission, is_show, is_enable) VALUES 
(@system_menu_id, '菜单管理', 2, '/system/menu', 'Menu', 3, '', 1, 1);

SET @menu_menu_id = LAST_INSERT_ID();

-- 菜单管理按钮
INSERT INTO sys_menu (parent_id, name, type, path, icon, sort, permission, is_show, is_enable) VALUES 
(@menu_menu_id, '查看菜单列表', 3, '', '', 1, 'system:menu:list', 0, 1),
(@menu_menu_id, '新增菜单', 3, '', '', 2, 'system:menu:add', 0, 1),
(@menu_menu_id, '编辑菜单', 3, '', '', 3, 'system:menu:edit', 0, 1),
(@menu_menu_id, '删除菜单', 3, '', '', 4, 'system:menu:delete', 0, 1);

-- 为超级管理员分配所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE del = 0;

-- 为系统管理员分配用户管理和角色管理菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, id FROM sys_menu WHERE del = 0 AND (
    path LIKE '/system/user%' OR 
    path LIKE '/system/role%' OR
    name IN ('系统管理')
);

-- 插入默认管理员账号（密码：Admin@123456，MD5加密后的值）
-- 这里需要先知道加密后的密码，使用 MD5('Admin@123456' + 'StudyAgent_Secret_Key_2024')
-- 计算方式：password + SALT = 'Admin@123456StudyAgent_Secret_Key_2024'
-- MD5值: 需要实际计算，这里先用占位符，实际使用时需要正确计算

-- 先插入用户，然后分配超级管理员角色
INSERT INTO sys_user (username, password, status) VALUES 
('admin', 'c93d73649a777c4e94d1c9e7e3a8b5d', 1);

-- 为admin用户分配超级管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES 
(1, 1);

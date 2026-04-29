# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

**学习辅助智能体 (Study Agent)** — 前后端分离的全栈项目

- 后端：Java 17 + Spring Boot 3.2.0 + MyBatis-Plus + MySQL
- 前端：Vue 3 + Vite 5 + Element Plus + Pinia
- AI：LangChain4j 集成 MiniMax/Claude 等 LLM，支持题目生成和错题分析

## 常用命令

### 后端 (Maven)
```bash
cd study-agent/backend
mvn spring-boot:run          # 运行 (端口 8080)
mvn package -DskipTests      # 打包
mvn test                      # 测试
```

### 前端 (npm)
```bash
cd study-agent/frontend
npm install                   # 安装依赖
npm run dev                   # 开发模式 (端口 3000，代理 /api 到 8080)
npm run build                 # 生产构建
npm run preview               # 预览构建
```

## 架构

```
前端 (3000) ──HTTP/Axios──> 后端 (8080) ──> MySQL
                          │
                          └──> LangChain4j ──> LLM (MiniMax/Claude)
                          └──> OCR (Tesseract/百度)
```

### 前端结构
- `views/student/` — 学生端页面 (练习、错题本、成就)
- `views/admin/` — 管理员端页面 (用户/角色/菜单管理)
- `store/*.js` — Pinia 状态管理
- `api/` — Axios API 调用封装

### 后端结构
- `controller/` — REST 控制器
- `service/` — 业务逻辑和 AI 服务
- `mapper/` — MyBatis-Plus 数据访问
- `entity/` — 数据实体
- `ai/` — AI 相关服务 (题目生成、错题分析)

### 核心功能
- **冲刺练习**：计时答题训练
- **AI 出题**：基于知识点自动生成题目
- **错题分析**：LLM 分析错误原因
- **成就系统**：学习成就解锁
- **RBAC 权限**：角色权限管理

## 配置

- 后端配置：`study-agent/backend/src/main/resources/application.yml`
- Vite 配置：`study-agent/frontend/vite.config.js`
- JWT 拦截器：`config/JwtInterceptor.java`
- LangChain4j 配置：`config/LangChain4jConfig.java`
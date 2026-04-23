# 学习辅助智能体 - Study Agent

让学习像玩游戏一样上瘾的 K12 学习辅助系统。

## 系统愿景

- 错题不只告诉"错了"，而是追踪到具体知识点薄弱点
- 练习题像游戏副本通关，做对题目获得奖励和成就感
- 学生能看到自己的"知识地图"和成长轨迹

## 技术架构

```
┌─────────────────────────────────────────────────────────┐
│                    Vue 3 + Vite                         │
│         (PC 管理后台 / H5 学生端自适应)                   │
└─────────────────────────────────────────────────────────┘
                           │ REST API
┌─────────────────────────────────────────────────────────┐
│                 Spring Boot 3.2                         │
│  学生服务 | 知识点服务 | 题库服务 | 奖励服务 | AI分析服务  │
└─────────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────────┐
│                   MySQL / Redis                         │
└─────────────────────────────────────────────────────────┘
```

## 核心功能

### 1. 学生与年级管理
- 学生账号、年级（小学/初中/高中）、班级
- 学习画像（各科知识点掌握度雷达图）

### 2. 知识点体系
- 树形结构：学科 → 章节 → 知识点 → 子知识点
- 难度等级（1-5 星）、常见错误类型

### 3. 题库管理
- 题目：题干、答案、解析、知识点标签、难度
- 错题本：自动归类学生错题

### 4. AI 错题分析
- 基于 LLM 分析错因（知识点缺失/审题不清/计算失误）
- 生成针对性练习建议

### 5. 游戏化奖励系统
- 💎 钻石 - 稀有货币（完成挑战获得）
- 🪙 金币 - 普通货币（答题奖励）
- 🏅 徽章 - 成就象征
- ⭐ 星级 - 知识点掌握度

### 6. 趣味练习模式
- 🏃 冲刺模式（限时答题）
- 🎯 精准打击（针对薄弱知识点）
- 🃏 卡牌对战（在线 PK）
- 🗺️ 知识地图（通关式推进）

## 项目结构

```
study-agent/
├── backend/                    # Spring Boot 后端
│   └── src/main/java/com/studyagent/
│       ├── entity/             # 实体类
│       ├── repository/         # 数据访问层
│       ├── service/            # 业务逻辑层
│       ├── controller/         # REST API 控制器
│       ├── ai/                 # AI 错题分析
│       ├── dto/                # 数据传输对象
│       └── config/             # 配置类
│
└── frontend/                   # Vue 3 前端
    └── src/
        ├── views/              # 页面组件
        ├── components/         # 公共组件
        ├── router/             # 路由配置
        ├── store/              # Pinia 状态管理
        ├── api/                # API 接口封装
        └── assets/             # 静态资源
```

## 数据库表

| 表名 | 说明 |
|------|------|
| t_student | 学生信息 |
| t_grade | 年级 |
| t_subject | 学科 |
| t_knowledge_point | 知识点 |
| t_question | 题目 |
| t_student_answer | 作答记录 |
| t_student_knowledge_mastery | 知识点掌握度 |
| t_achievement | 成就定义 |
| t_student_achievement | 学生已获得成就 |
| t_daily_task | 每日任务 |
| t_student_daily_task | 学生每日任务进度 |

## 快速开始

### 后端启动

```bash
cd backend
# 配置 MySQL 和 Redis
# 修改 application.yml 中的数据库连接
mvn spring-boot:run
```

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

### 配置 OpenAI API

在 `application.yml` 中配置：

```yaml
spring:
  ai:
    openai:
      api-key: your-api-key
```

## API 列表

### 学生端
- `POST /api/student/register` - 注册
- `POST /api/student/login` - 登录
- `GET /api/student/{id}` - 获取学生信息
- `POST /api/question/submit` - 提交答案
- `GET /api/question/wrong/{studentId}` - 获取错题
- `GET /api/reward/achievements/{studentId}` - 获取成就

### 管理端
- `CRUD /api/admin/student` - 学生管理
- `CRUD /api/admin/knowledge` - 知识点管理
- `CRUD /api/admin/question` - 题库管理

## 游戏化奖励计算

```java
int baseGold = 10;
int baseExp = 5;

// 连击加成 (每连击 +20%)
double comboMultiplier = 1 + (comboCount * 0.2);

// 难度加成 (每星难度 +10%)
double difficultyBonus = 1 + (difficulty * 0.1);

// 最终奖励
int finalGold = (int)(baseGold * comboMultiplier * difficultyBonus);
```

## 下一步计划

### Phase 1 ✅ 已完成
- [x] 学生管理 + 知识点体系
- [x] 题库管理 + 手动录入
- [x] 简单错题分析（基于知识点标签）
- [x] 基础奖励系统（金币、经验值）

### Phase 2 规划中
- [ ] 接入 LLM 做错题深度分析
- [ ] 智能练习推荐算法
- [ ] 学习画像和进度可视化

### Phase 3 规划中
- [ ] 多种练习模式（冲刺、对战、通关）
- [ ] 排行榜系统
- [ ] 徽章成就系统
- [ ] 社交功能（班级 PK）

## 学习资源

- [Duolingo](https://duolingo.com) - 游戏化语言学习典范
- [Khan Academy](https://khanacademy.org) - 可汗学院
- Spring Boot 3.2 + JPA + MySQL
- Vue 3 + Vite + Element Plus + Pinia

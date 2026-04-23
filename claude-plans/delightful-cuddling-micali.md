# 学习辅助智能体系统设计

## Context

用户想要构建一个 K12 学习辅助智能体系统，旨在：
1. 根据学生年级和教学进度明确应掌握的知识点
2. 录入作业和考试卷，分析错题对应的知识点缺陷
3. 生成趣味性练习（游戏化奖励机制）
4. 技术栈：SpringBoot + Vue，支持 PC 和 H5

## 系统愿景

**核心价值**：让学习像玩游戏一样上瘾

- 错题不只告诉"错了"，而是追踪到具体知识点薄弱点
- 练习题像游戏副本通关，做对题目获得奖励和成就感
- 学生能看到自己的"知识地图"和成长轨迹

---

## 系统架构设计

### 技术架构

```
┌─────────────────────────────────────────────────────────┐
│                    Vue 3 + Vite                         │
│         (PC 管理后台 / H5 学生端自适应)                   │
└─────────────────────────────────────────────────────────┘
                           │ REST API
┌─────────────────────────────────────────────────────────┐
│                 Spring Boot 微服务架构                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐ │
│  │ 学生服务  │  │ 知识点服务 │  │ 题库服务  │  │ 奖励服务 │ │
│  └──────────┘  └──────────┘  └──────────┘  └─────────┘ │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │ AI 分析服务 │  │ 练习服务  │  │ 进度服务  │              │
│  └──────────┘  └──────────┘  └──────────┘              │
└─────────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────────┐
│                   MySQL / Redis                         │
│     (结构化数据 / 缓存 / 会话)                            │
└─────────────────────────────────────────────────────────┘
```

### 核心模块设计

#### 1. 学生与年级管理
- 学生账号、年级（小学/初中/高中）、班级
- 教学进度跟踪（按教材章节）
- 学习画像（各科知识点掌握度雷达图）

#### 2. 知识点体系
- **树形结构**：学科 → 章节 → 知识点 → 子知识点
- **知识点属性**：
  - 名称、编码、描述
  - 对应年级、教材版本
  - 难度等级（1-5 星）
  - 常见错误类型
  - 关联题目特征

#### 3. 题库管理
- 题目：题干、答案、解析、知识点标签、难度、题型
- 作业/试卷录入：支持手动录入和拍照 OCR
- 错题本：自动归类学生错题

#### 4. AI 错题分析（核心能力）
```
输入：学生作答记录（含错题）
      ↓
LLM 分析：
  - 这道题考察什么知识点？
  - 错误原因是知识点没掌握？还是审题问题？计算失误？
  - 该知识点的掌握程度如何？
      ↓
输出：
  - 错因分类：知识点缺失 / 审题不清 / 计算错误 / 理解偏差
  - 知识点掌握度评分
  - 针对性的微练习建议
```

#### 5. 游戏化奖励系统
| 元素 | 说明 | 获取方式 |
|------|------|----------|
| 💎 钻石 | 稀有货币 | 完成挑战、连续打卡 |
| 🪙 金币 | 普通货币 | 做对题目、日常任务 |
| 🏅 徽章 | 成就象征 | 攻克难点、连胜、里程碑 |
| ⭐ 星级 | 知识点星级 | 掌握一个知识点升一星 |
| 📊 排行榜 | 排名激励 | 按金币、连胜、掌握度 |
| 🎁 宝箱 | 随机奖励 | 连续完成练习、等级提升 |

#### 6. 趣味练习生成
- **练习模式**：
  - 🏃 冲刺模式（限时答题）
  - 🎯 精准打击（针对薄弱知识点）
  - 🃏 卡牌对战（PK 答题）
  - 🗺️ 知识地图（通关式推进）
- **即时反馈**：答对有特效动画 + 奖励弹窗
- **连击机制**：连续答对增加奖励倍数

#### 7. 练习推荐引擎
- 根据错题分析结果生成练习计划
- 优先推送：高频考点 + 学生薄弱点
- 难度螺旋上升
- 支持自定义练习（家长/老师布置）

---

## 数据模型

### 核心实体

```
Student (学生)
├── id, name, grade, class_id
├── avatar, nickname
└── exp,金币,钻石,连续学习天数

Grade (年级)
├── id, name (小一/初一/高一)
├── stage (小学/初中/高中)
└── subject_order[]

Subject (学科)
├── id, name, grade_id
└── textbook_version

KnowledgePoint (知识点)
├── id, name, code
├── parent_id (树形)
├── subject_id, grade_id
├── difficulty (1-5)
├── common_errors[] (常见错误类型)
└── related_concepts[]

Question (题目)
├── id, type (选择/填空/解答)
├── content, answer, analysis
├── knowledge_point_ids[]
├── difficulty, frequency
└── error_patterns[]

StudentAnswer (作答记录)
├── id, student_id, question_id
├── answer, is_correct
├── error_type (如果错)
└── answered_at

Achievement (成就/徽章)
├── id, name, icon, description
├── condition_type, condition_value
└── reward

StudentAchievement (学生已获得成就)
├── student_id, achievement_id
└── earned_at
```

---

## API 设计（REST）

### 学生端 API
```
GET  /api/student/profile          # 获取学生信息
GET  /api/student/knowledge-radar  # 知识点掌握度雷达图
GET  /api/student/daily-tasks      # 每日任务
POST /api/answer/submit             # 提交答案
GET  /api/practice/recommend       # 获取推荐练习
POST /api/practice/start           # 开始练习
GET  /api/achievement/list         # 我的成就
GET  /api/rank/week                # 周排行榜
```

### 管理端 API
```
CRUD /api/admin/student            # 学生管理
CRUD /api/admin/knowledge          # 知识点管理
CRUD /api/admin/question           # 题库管理
POST /api/admin/exam/upload        # 试卷上传/录入
GET  /api/admin/report/class       # 班级学情报告
```

---

## 关键实现细节

### AI 错题分析提示词设计

```
你是一个专业的 K12 数学老师。请分析以下错题：

题目：{question_content}
学生答案：{student_answer}
正确答案：{correct_answer}

请分析：
1. 这道题考察的知识点是什么？（给出知识点名称）
2. 学生的错误原因是什么？
   - 知识点完全没掌握
   - 知识点部分理解
   - 审题理解错误
   - 计算失误
   - 其他（请说明）
3. 针对这个知识点，推荐什么类型的练习题？

请用 JSON 格式返回：
{
  "knowledge_point": "xxx",
  "error_type": "知识点缺失|理解偏差|审题错误|计算失误",
  "mastery_level": 0.3,
  "practice_suggestion": "建议做 X 道 xxx 类型的题目"
}
```

### 游戏化奖励计算

```java
// 答对题目基础奖励
int baseGold = 10;
int baseExp = 5;

// 连击加成
double comboMultiplier = 1 + (comboCount * 0.2); // 20% 递增

// 难度加成
double difficultyBonus = 1 + (question.getDifficulty() * 0.1);

// 最终奖励
int finalGold = (int)(baseGold * comboMultiplier * difficultyBonus);
int finalExp = (int)(baseExp * comboMultiplier * difficultyBonus);
```

---

## 验证计划

1. **单元测试**：Service 层核心逻辑（奖励计算、知识点匹配）
2. **API 测试**：Postman 验证各接口
3. **集成测试**：学生作答 -> AI 分析 -> 生成练习 -> 发放奖励 全流程
4. **前端验证**：PC 和 H5 响应式布局检查

---

## 优先级排序

### Phase 1：基础功能（MVP）
1. 学生管理 + 知识点体系（树形）
2. 题库管理 + 手动录入
3. 简单的错题分析（基于知识点标签匹配，非 LLM）
4. 基础奖励系统（金币、经验值）

### Phase 2：智能化
1. 接入 LLM 做错题深度分析
2. 智能练习推荐算法
3. 学习画像和进度可视化

### Phase 3：游戏化增强
1. 多种练习模式
2. 排行榜系统
3. 徽章成就系统
4. 社交功能（班级 PK）

---

## 参考资料

- 游戏化学习案例：Duolingo（语言学习）、Khan Academy
- K12 知识点体系：人教版教材大纲

# 冲刺模式 - 详细产品设计

## 1. 功能概述

### 1.1 模式定义
**🏃 冲刺模式（Sprint Mode）** — 在限定时间内，以最快速度、最高准确率完成尽可能多的题目，考验学生的瞬间反应和知识掌握程度。

### 1.2 设计理念
- **速度与激情**：60 秒限时挑战，营造紧张刺激的学习氛围
- **即时反馈**：每题即时判定，连击奖励机制激励连续正确
- **可视化成长**：实时显示得分、连击、时间，激发胜负欲

### 1.3 核心指标

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 单轮时长 | 60 秒 | 固定时间，不可调整 |
| 题目数量 | 不限 | 时间范围内尽可能多答 |
| 最低难度 | 1 星 | 可出现 1-5 星难度 |
| 最高连击 | 无上限 | 连击越高奖励越多 |
| 结算延迟 | < 500ms | 即时结算反馈 |

---

## 2. 核心规则

### 2.1 计时规则

```
┌────────────────────────────────────────────────────────┐
│                    60 秒冲刺倒计时                       │
├────────────────────────────────────────────────────────┤
│                                                        │
│  [████████████████████████████░░░░░░░░░░░░░░░░░░░░░]   │
│                                                        │
│  已用时：00:15            剩余时间：00:45               │
│                                                        │
│  时间分段加成：                                         │
│  ├─ 00:00 - 00:30   → 正常奖励 × 1.0                  │
│  ├─ 00:30 - 00:45   → 正常奖励 × 1.2  (+20% 加速)     │
│  └─ 00:45 - 01:00   → 正常奖励 × 1.5  (+50% 冲刺)     │
│                                                        │
└────────────────────────────────────────────────────────┘
```

| 时间段 | 加成系数 | 设计意图 |
|--------|----------|----------|
| 0-30 秒 | × 1.0 | 热身阶段，稳中求进 |
| 30-45 秒 | × 1.2 | 加速阶段，鼓励提速 |
| 45-60 秒 | × 1.5 | 冲刺阶段，最后拼搏 |

### 2.2 连击机制

| 连击数 | 额外奖励 | 解锁成就条件 |
|--------|----------|--------------|
| 3 连击 | +1 💎 | - |
| 5 连击 | +5 💎 + 解锁「连胜新星」检查点 | 可领取成就 |
| 10 连击 | +20 💎 + 解锁「连胜达人」检查点 | 顶级成就 |
| 20 连击 | +50 💎 | 特殊标记「无敌」 |

**连击断结算规则：**
- 答错 → 连击归零，但不结束本轮
- 超时未作答 → 视为答错，连击归零

### 2.3 得分计算公式

```
单题得分 = 基础分 × 时间加成系数 × 连击加成系数

其中：
  基础分 = 100 分（固定）
  时间加成系数 = 1.0 / 1.2 / 1.5（根据答题时间所处区间）
  连击加成系数 = 1.0 + (连击数 × 0.1)，封顶 3.0

本轮总得分 = Σ(每题得分)
```

**得分示例：**

| 场景 | 基础分 | 时间加成 | 连击加成 | 单题得分 |
|------|--------|----------|----------|----------|
| 第 1 题，10 秒答对 | 100 | × 1.0 | × 1.0 | 100 |
| 第 5 题，35 秒答对（4 连击） | 100 | × 1.2 | × 1.4 | 168 |
| 第 10 题，50 秒答对（9 连击） | 100 | × 1.5 | × 1.9 | 285 |

### 2.4 奖励计算

```
答对奖励：
  金币 = 10 × (1 + 连击数 × 0.2) × (1 + 难度星级 × 0.1)
  经验 = 5 × (1 + 连击数 × 0.2) × (1 + 难度星级 × 0.1)
  钻石 = 0（钻石只在特定连击时发放）

特殊连击奖励：
  5 连击：+5 钻石
  10 连击：+20 钻石
  20 连击：+50 钻石

答错惩罚：
  无金币/经验扣除
  连击归零
```

---

## 3. 题目机制

### 3.1 题目来源

| 来源 | 占比 | 说明 |
|------|------|------|
| 学生所在年级 | 100% | 根据学生的 gradeId 筛选 |
| 学生选择科目 | 100% | 可选语文/数学/英语 |
| 随机抽取 | - | 不重复抽取同一题目 |

### 3.2 题目筛选优先级

```
优先级 1：高频考点题目（frequency 高的优先）
优先级 2：近期未练习的题目
优先级 3：学生薄弱知识点的相关题目
优先级 4：随机抽取
```

### 3.3 难度分布

| 星级 | 占比 | 说明 |
|------|------|------|
| ★☆☆☆☆ (1星) | 40% | 基础题，送分题 |
| ★★☆☆☆ (2星) | 30% | 简单题 |
| ★★★☆☆ (3星) | 20% | 中等题 |
| ★★★★☆ (4星) | 8% | 较难题 |
| ★★★★★ (5星) | 2% | 极难题，高风险高回报 |

### 3.4 题目类型

| 类型 | 占比 | 答题方式 |
|------|------|----------|
| 选择题 | 70% | 四选一，点击选择 |
| 填空题 | 30% | 键盘输入答案 |

---

## 4. 页面设计

### 4.1 开始页面

```
┌─────────────────────────────────────────────────────────┐
│                                                         │
│                    🏃 冲刺模式                           │
│                                                         │
│              「60 秒限时挑战，考验你的速度！」             │
│                                                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │                                                 │    │
│  │     🏆 历史最佳得分：12,500 分                   │    │
│  │     📊 历史最高连击：15 连                       │    │
│  │     🎯 历史最高正确率：95%                       │    │
│  │                                                 │    │
│  └─────────────────────────────────────────────────┘    │
│                                                         │
│  选择科目：                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │   📚    │  │   🔢    │  │   🗣️    │              │
│  │   语文   │  │   数学   │  │   英语   │              │
│  └──────────┘  └──────────┘  └──────────┘              │
│                                                         │
│                                                         │
│           ┌─────────────────────────────┐                │
│           │                             │                │
│           │      🚀 开始冲刺            │                │
│           │                             │                │
│           └─────────────────────────────┘                │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**页面元素说明：**
- 历史最佳数据从后端获取，无记录时显示"--"
- 科目卡片：选中态有边框高亮，语文/数学/英语三选一
- 开始按钮：渐变背景，hover 有上浮动效

### 4.2 答题页面

```
┌─────────────────────────────────────────────────────────┐
│  🏃 冲刺模式                            🔥 8 连击       │
│                                        📊 1,680 分      │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ⏱️ 00:38                                              │
│  ═══════════════════════════░░░░░░░░░░░░░░░░░░░░░░░   │
│                                                         │
│  第 12 题                                               │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │                                                  │   │
│  │  2 + 3 × 4 = ?                                  │   │
│  │                                                  │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────┐  ┌─────────────┐                      │
│  │      A      │  │      B      │                      │
│  │     14      │  │     20      │                      │
│  └─────────────┘  └─────────────┘                      │
│  ┌─────────────┐  ┌─────────────┐                      │
│  │      C      │  │      D      │                      │
│  │     24      │  │     10      │                      │
│  └─────────────┘  └─────────────┘                      │
│                                                         │
│  ─────────────────────────────────────────────────────  │
│                                                         │
│              🟢🟢🟢🟢🟢🟢🟢🔴🔴🔴                      │
│              已答 10 / 正确 8 / 错误 2                   │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**页面元素说明：**

| 元素 | 位置 | 说明 |
|------|------|------|
| 模式标签 | 左上 | 显示当前模式 |
| 连击数 | 右上 | 连续答对题数，有火焰动画 |
| 实时得分 | 右上 | 红色字体，持续累加 |
| 倒计时 | 中上 | 大字体，红色警告（<10秒） |
| 时间进度条 | 倒计时下方 | 渐变色，随时间减少 |
| 题号 | 中部 | "第 X 题" |
| 题目内容 | 中部 | 题干文字 |
| 选项卡片 | 中部 | 4 选 1，点击即提交 |
| 答题进度 | 底部 | 绿/红小点表示对/错 |

**交互细节：**

| 交互 | 行为 |
|------|------|
| 点击选项 | 立即判定对错，显示结果（1秒），自动加载下一题 |
| 答对 | 选项变绿 + 播放音效 + 连击+1 + 得分飞字动画 |
| 答错 | 选项变红 + 正确选项变绿 + 连击归零 |
| 时间耗尽 | 弹出结算页面 |

### 4.3 结算页面

```
┌─────────────────────────────────────────────────────────┐
│                                                         │
│                    🏁 时间到！                           │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │                                                  │   │
│  │              本 轮 成 绩                          │   │
│  │                                                  │   │
│  │         ┌─────────────────────┐                 │   │
│  │         │     2,850 分        │                 │   │
│  │         │     🏆 新纪录！      │                 │   │
│  │         └─────────────────────┘                 │   │
│  │                                                  │   │
│  │   ──────────────────────────────────────────    │   │
│  │                                                  │   │
│  │   📝 总题数    🟢 正确    🔴 错误    🔥 最高连击 │   │
│  │     18        15        3          8           │   │
│  │                                                  │   │
│  │   ⏱️ 用时    📊 正确率    ⭐ 难度分布           │   │
│  │   60 秒      83.3%      ★★☆☆☆               │   │
│  │                                                  │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │              🎁 本轮奖励                         │   │
│  │                                                  │   │
│  │    🪙 +180 金币    ⭐ +95 经验    💎 +5 钻石    │   │
│  │                                                  │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │         🏆 新成就：「连胜新星」                   │   │
│  │         首次达成 5 连击！                        │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│        ┌─────────────────┐  ┌─────────────────┐         │
│        │   🚀 再来一局    │  │    🏠 返回首页   │         │
│        └─────────────────┘  └─────────────────┘         │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**页面元素说明：**

| 元素 | 说明 |
|------|------|
| 新纪录标识 | 分数超过历史最佳时显示 |
| 统计数据 | 总题数/正确/错误/最高连击 |
| 奖励展示 | 金币/经验/钻石累加动画 |
| 新成就卡片 | 有新成就时显示领取动画 |

---

## 5. 后端 API 设计

### 5.1 API 列表

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/practice/sprint/start` | 开始冲刺 |
| POST | `/api/practice/sprint/answer` | 提交答案 |
| GET | `/api/practice/sprint/question` | 获取下一题 |
| POST | `/api/practice/sprint/finish` | 结束冲刺 |
| GET | `/api/practice/sprint/history` | 获取冲刺历史 |
| GET | `/api/practice/sprint/best` | 获取最佳记录 |

### 5.2 API 详细设计

#### 5.2.1 开始冲刺

```
POST /api/practice/sprint/start
Content-Type: application/json

{
  "studentId": 1,
  "subjectId": 3    // 可选，不传则随机科目
}

Response 200:
{
  "success": true,
  "data": {
    "sessionId": "sprint_20240417_001",
    "startTime": "2024-04-17T14:30:00",
    "duration": 60,
    "subjectId": 3,
    "question": {
      "id": 101,
      "type": "选择",
      "content": "2 + 3 = ?",
      "options": ["A. 4", "B. 5", "C. 6", "D. 7"],
      "difficulty": 1
    }
  }
}
```

#### 5.2.2 提交答案

```
POST /api/practice/sprint/answer
Content-Type: application/json

{
  "sessionId": "sprint_20240417_001",
  "studentId": 1,
  "questionId": 101,
  "answer": "B",
  "answerTime": 3.5    // 答题用时（秒）
}

Response 200:
{
  "success": true,
  "data": {
    "isCorrect": true,
    "correctAnswer": "B",
    "comboCount": 5,
    "timeBonus": 1.0,
    "comboBonus": 1.8,
    "score": 180,
    "reward": {
      "gold": 28,
      "exp": 14,
      "diamond": 1
    },
    "achievementUnlocked": {    // 有新成就时返回
      "id": 4,
      "name": "连胜新星",
      "icon": "⭐"
    },
    "nextQuestion": {
      "id": 102,
      "type": "填空",
      "content": "5 - 2 = __",
      "difficulty": 1
    }
  }
}
```

#### 5.2.3 结束冲刺

```
POST /api/practice/sprint/finish
Content-Type: application/json

{
  "sessionId": "sprint_20240417_001",
  "studentId": 1,
  "reason": "TIMEOUT"    // TIMEOUT | USER_END | SURRENDER
}

Response 200:
{
  "success": true,
  "data": {
    "sessionId": "sprint_20240417_001",
    "totalScore": 2850,
    "totalQuestions": 18,
    "correctCount": 15,
    "wrongCount": 3,
    "accuracy": 83.3,
    "maxCombo": 8,
    "duration": 60,
    "rewards": {
      "gold": 180,
      "exp": 95,
      "diamond": 5
    },
    "newAchievements": [
      {
        "id": 4,
        "name": "连胜新星",
        "icon": "⭐"
      }
    ],
    "isNewRecord": true,
    "previousBest": 2400,
    "historyRank": 1
  }
}
```

#### 5.2.4 获取冲刺历史

```
GET /api/practice/sprint/history?studentId=1&limit=10

Response 200:
{
  "success": true,
  "data": [
    {
      "sessionId": "sprint_20240417_001",
      "date": "2024-04-17T14:30:00",
      "subjectName": "英语",
      "totalScore": 2850,
      "totalQuestions": 18,
      "correctCount": 15,
      "accuracy": 83.3,
      "maxCombo": 8,
      "duration": 60
    },
    {
      "sessionId": "sprint_20240416_003",
      "date": "2024-04-16T19:45:00",
      "subjectName": "数学",
      "totalScore": 2400,
      "totalQuestions": 16,
      "correctCount": 14,
      "accuracy": 87.5,
      "maxCombo": 6,
      "duration": 55
    }
  ]
}
```

#### 5.2.5 获取最佳记录

```
GET /api/practice/sprint/best?studentId=1

Response 200:
{
  "success": true,
  "data": {
    "bestScore": 2850,
    "bestCombo": 12,
    "bestAccuracy": 95.0,
    "totalSessions": 28,
    "totalQuestions": 456,
    "totalCorrect": 398
  }
}
```

---

## 6. 数据库设计

### 6.1 新增数据表

#### 6.1.1 冲刺记录表 `t_sprint_record`

```sql
CREATE TABLE t_sprint_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    session_id VARCHAR(50) NOT NULL DEFAULT '' COMMENT '会话ID',
    student_id BIGINT NOT NULL DEFAULT 0 COMMENT '学生ID',
    subject_id BIGINT NOT NULL DEFAULT 0 COMMENT '学科ID',
    total_score INT NOT NULL DEFAULT 0 COMMENT '总得分',
    total_questions INT NOT NULL DEFAULT 0 COMMENT '总题数',
    correct_count INT NOT NULL DEFAULT 0 COMMENT '正确数',
    wrong_count INT NOT NULL DEFAULT 0 COMMENT '错误数',
    accuracy DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '正确率',
    max_combo INT NOT NULL DEFAULT 0 COMMENT '最高连击',
    duration INT NOT NULL DEFAULT 0 COMMENT '实际用时（秒）',
    end_reason VARCHAR(20) NOT NULL DEFAULT '' COMMENT '结束原因：TIMEOUT/USER_END/SURRENDER',
    is_new_record TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否创新纪录',
    del TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_session (session_id),
    INDEX idx_student (student_id),
    INDEX idx_student_date (student_id, create_time)
) COMMENT='冲刺模式记录表';
```

#### 6.1.2 冲刺会话表 `t_sprint_session`

```sql
CREATE TABLE t_sprint_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    session_id VARCHAR(50) NOT NULL DEFAULT '' COMMENT '会话ID',
    student_id BIGINT NOT NULL DEFAULT 0 COMMENT '学生ID',
    subject_id BIGINT NOT NULL DEFAULT 0 COMMENT '学科ID',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/FINISHED/EXPIRED',
    used_question_ids TEXT NOT NULL COMMENT '已使用题目ID，JSON格式',
    del TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_session (session_id),
    INDEX idx_student (student_id),
    INDEX idx_status (status)
) COMMENT='冲刺模式会话表';
```

### 6.2 实体类设计

#### SprintRecord.java
```java
@Data
@TableName("t_sprint_record")
public class SprintRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionId;
    private Long studentId;
    private Long subjectId;
    private Integer totalScore;
    private Integer totalQuestions;
    private Integer correctCount;
    private Integer wrongCount;
    private Double accuracy;
    private Integer maxCombo;
    private Integer duration;
    private String endReason;
    private Boolean isNewRecord;

    @TableLogic
    private Integer del = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;
}
```

#### SprintSession.java
```java
@Data
@TableName("t_sprint_session")
public class SprintSession {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionId;
    private Long studentId;
    private Long subjectId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String usedQuestionIds;  // JSON 格式

    @TableLogic
    private Integer del = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;
}
```

### 6.3 新增 Mapper

```java
@Mapper
public interface SprintRecordMapper extends BaseMapper<SprintRecord> {}

@Mapper
public interface SprintSessionMapper extends BaseMapper<SprintSession> {}
```

---

## 7. 前端组件设计

### 7.1 路由配置

```javascript
{
  path: '/student/sprint',
  name: 'SprintMode',
  component: () => import('@/views/student/SprintMode.vue')
}
```

### 7.2 组件结构

```
views/student/
├── SprintStart.vue      # 开始页面
├── SprintPlay.vue       # 答题页面
└── SprintResult.vue     # 结算页面
```

### 7.3 状态管理

```javascript
// store/sprint.js
export const useSprintStore = defineStore('sprint', {
  state: () => ({
    sessionId: null,
    status: 'idle',       // idle | playing | finished
    subjectId: null,
    startTime: null,
    duration: 60,
    currentQuestion: null,
    currentCombo: 0,
    currentScore: 0,
    questionCount: 0,
    correctCount: 0,
    wrongCount: 0,
    maxCombo: 0,
    rewards: { gold: 0, exp: 0, diamond: 0 },
    historyBest: { score: 0, combo: 0, accuracy: 0 }
  }),

  getters: {
    accuracy: (state) => {
      if (state.questionCount === 0) return 0
      return (state.correctCount / state.questionCount * 100).toFixed(1)
    }
  },

  actions: {
    async startSprint(subjectId) { /* ... */ },
    async submitAnswer(answer, answerTime) { /* ... */ },
    async finishSprint(reason) { /* ... */ },
    reset() { /* ... */ }
  }
})
```

---

## 8. 音效与动画设计

### 8.1 音效清单

| 音效 | 触发时机 | 说明 |
|------|----------|------|
| correct.mp3 | 答对 | 轻快的成功音效 |
| wrong.mp3 | 答错 | 低沉的成功音效 |
| combo_5.mp3 | 达成 5 连击 | 特别庆祝音效 |
| combo_10.mp3 | 达成 10 连击 | 史诗级庆祝音效 |
| timeout.mp3 | 时间耗尽 | 倒计时结束音效 |
| new_record.mp3 | 创新纪录 | 特殊庆祝音效 |

### 8.2 动画效果

| 动画 | 元素 | 说明 |
|------|------|------|
| 得分飞字 | +100 | 答对后数字飘向得分 |
| 连击放大 | 🔥 8 | 连击数放大闪烁 |
| 选项震动 | 选错时 | 错误选项抖动 |
| 时间加速 | 剩余15秒 | 倒计时加速红色闪烁 |
| 新成就弹出 | 成就卡片 | 从底部弹入 |

---

## 9. 成就联动

### 9.1 新增成就

| 成就ID | 名称 | 图标 | 条件 | 奖励 |
|--------|------|------|------|------|
| SPRINT_NEWBIE | 初次冲刺 | 🏃 | 完成首次冲刺 | 20金币, 10经验 |
| SPRINT_5 | 冲刺新星 | ⭐ | 单轮答对5题 | 50金币, 25经验 |
| SPRINT_10 | 冲刺达人 | 🌟 | 单轮答对10题 | 100金币, 50经验, 3钻石 |
| SPRINT_PERFECT | 完美冲刺 | 💯 | 正确率100% | 200金币, 100经验, 10钻石 |
| SPRINT_COMBO_10 | 连击冲刺 | 🔥 | 单轮10连击 | 150金币, 75经验, 5钻石 |
| SPRINT_SCORE_2000 | 2000分 | 🏆 | 单轮得分2000+ | 300金币, 150经验, 15钻石 |
| SPRINT_SCORE_3000 | 3000分 | 👑 | 单轮得分3000+ | 500金币, 250经验, 30钻石 |

### 9.2 成就检查时机

```
每次答题后检查：
- SPRINT_NEWBIE（首次完成）
- SPRINT_5, SPRINT_10（累计答对数）
- SPRINT_COMBO_10（当前连击数）

冲刺结束时检查：
- SPRINT_PERFECT（正确率100%）
- SPRINT_SCORE_2000, SPRINT_SCORE_3000（总得分）
```

---

## 10. 异常处理

### 10.1 网络异常

| 场景 | 处理方式 |
|------|----------|
| 提交答案网络超时 | 显示重试按钮，答题状态保留 |
| 服务器异常 | 显示错误提示，可选择重新开始 |
| 页面刷新 | 通过 sessionId 恢复会话 |

### 10.2 边界情况

| 场景 | 处理方式 |
|------|----------|
| 题库题目不足 | 循环使用题目，允许重复 |
| 60秒内一题未答 | 结算页面显示"未开始" |
| 同时开启多个会话 | 禁止，后一个覆盖前一个 |

---

## 11. 性能要求

| 指标 | 要求 |
|------|------|
| 页面加载时间 | < 2 秒 |
| 题目加载时间 | < 500ms |
| 答案提交响应 | < 300ms |
| 结算页面渲染 | < 1 秒 |
| 同时在线会话 | 支持 1000+ 并发 |

---

## 12. 数据统计埋点

### 12.1 埋点事件

| 事件名 | 触发时机 | 携带数据 |
|--------|----------|----------|
| sprint_start | 开始冲刺 | studentId, subjectId, time |
| sprint_answer | 每题提交 | questionId, isCorrect, answerTime, combo |
| sprint_finish | 结束冲刺 | totalScore, accuracy, maxCombo, duration |
| sprint_quit | 主动退出 | answeredCount, currentScore |

### 12.2 运营数据

- 每日冲刺次数
- 平均冲刺时长
- 各科目冲刺分布
- 连击分布统计
- 得分分布区间

---

## 13. 实现优先级

### Phase 1 - MVP（最小可行版本）
1. 开始页面（选择科目、显示历史最佳）
2. 答题页面（倒计时、答题、判定对错）
3. 结算页面（显示成绩和奖励）
4. 后端 API 实现
5. 数据库表创建

### Phase 2 - 增强体验
1. 连击动画和音效
2. 得分飞字动画
3. 新成就系统接入
4. 历史记录页面

### Phase 3 - 优化完善
1. 题目推荐算法优化
2. 排行榜功能
3. 社交分享功能
4. 音效开关设置

---

## 14. 附录

### 14.1 参考实现

- **Duolingo** - 冲刺模式的灵感来源
- **答题游戏** - 速度挑战机制

### 14.2 术语表

| 术语 | 说明 |
|------|------|
| Session | 一次冲刺会话 |
| Combo | 连续答对题数 |
| Score | 冲刺得分 |
| Time Bonus | 时间加成 |
| Sprint | 冲刺模式 |

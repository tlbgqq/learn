# 冲刺模式实现计划

## Context

冲刺模式是一个限时 60 秒的答题挑战模式，玩家需要在时间内尽可能多答对题目。当前首页有入口但未实现。需要完整实现前后端功能。

**参考文档：** `study-agent/docs/SPRINT_MODE_DESIGN.md`

---

## 实现范围

### 后端（Spring Boot）
### 前端（Vue 3）
### 数据库（MySQL）

---

## 后端实现

### 1. 数据表 SQL

**文件：** `backend/src/main/resources/schema.sql`（追加）

```sql
-- 冲刺记录表
CREATE TABLE t_sprint_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(50) NOT NULL DEFAULT '',
    student_id BIGINT NOT NULL DEFAULT 0,
    subject_id BIGINT NOT NULL DEFAULT 0,
    total_score INT NOT NULL DEFAULT 0,
    total_questions INT NOT NULL DEFAULT 0,
    correct_count INT NOT NULL DEFAULT 0,
    wrong_count INT NOT NULL DEFAULT 0,
    accuracy DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    max_combo INT NOT NULL DEFAULT 0,
    duration INT NOT NULL DEFAULT 0,
    end_reason VARCHAR(20) NOT NULL DEFAULT '',
    is_new_record TINYINT(1) NOT NULL DEFAULT 0,
    del TINYINT(1) NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_session (session_id),
    INDEX idx_student (student_id)
) COMMENT='冲刺模式记录表';

-- 冲刺会话表
CREATE TABLE t_sprint_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(50) NOT NULL DEFAULT '',
    student_id BIGINT NOT NULL DEFAULT 0,
    subject_id BIGINT NOT NULL DEFAULT 0,
    start_time DATETIME NOT NULL,
    end_time DATETIME DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    used_question_ids TEXT NOT NULL,
    del TINYINT(1) NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_session (session_id),
    INDEX idx_student (student_id)
) COMMENT='冲刺模式会话表';
```

### 2. 实体类

**新建文件：**
- `entity/SprintRecord.java`
- `entity/SprintSession.java`

**模式参考：** `entity/Question.java`
- 使用 `@Data`、`@TableName`
- 软删除：`@TableLogic`
- 时间戳：`@TableField(fill = FieldFill.INSERT)`

### 3. Mapper

**新建文件：**
- `mapper/SprintRecordMapper.java` - 继承 `BaseMapper<SprintRecord>`
- `mapper/SprintSessionMapper.java` - 继承 `BaseMapper<SprintSession>`

**模式参考：** `mapper/QuestionMapper.java`

### 4. DTO 类

**新建文件：**
- `dto/SprintStartRequest.java` - 开始冲刺请求
- `dto/SprintAnswerRequest.java` - 提交答案请求
- `dto/SprintFinishRequest.java` - 结束冲刺请求
- `dto/SprintQuestionResponse.java` - 题目响应
- `dto/SprintAnswerResponse.java` - 答案响应
- `dto/SprintResultResponse.java` - 结算响应
- `dto/SprintHistoryResponse.java` - 历史记录响应

### 5. Service

**新建文件：** `service/SprintService.java`

**核心逻辑：**
- `startSprint(studentId, subjectId)` - 创建会话，返回第一题
- `getNextQuestion(sessionId)` - 获取下一题（不重复）
- `submitAnswer(sessionId, questionId, answer, answerTime)` - 判定答案，计算奖励
- `finishSprint(sessionId, reason)` - 结束会话，保存记录
- `getHistory(studentId, limit)` - 获取历史记录
- `getBestRecord(studentId)` - 获取最佳记录

**计算公式（参考设计文档）：**
```java
// 时间加成
timeBonus = elapsed < 30 ? 1.0 : (elapsed < 45 ? 1.2 : 1.5)

// 连击加成
comboBonus = Math.min(1.0 + combo * 0.1, 3.0)

// 单题得分
score = 100 * timeBonus * comboBonus

// 金币奖励
gold = 10 * (1 + combo * 0.2) * (1 + difficulty * 0.1)

// 经验奖励
exp = 5 * (1 + combo * 0.2) * (1 + difficulty * 0.1)

// 钻石奖励
diamond = combo >= 20 ? 50 : combo >= 10 ? 20 : combo >= 5 ? 5 : combo >= 3 ? 1 : 0
```

### 6. Controller

**新建文件：** `controller/SprintController.java`

**API 端点：**
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/practice/sprint/start` | 开始冲刺 |
| GET | `/api/practice/sprint/question` | 获取下一题 |
| POST | `/api/practice/sprint/answer` | 提交答案 |
| POST | `/api/practice/sprint/finish` | 结束冲刺 |
| GET | `/api/practice/sprint/history` | 获取历史 |
| GET | `/api/practice/sprint/best` | 获取最佳 |

**模式参考：** `controller/QuestionController.java`

---

## 前端实现

### 1. 路由配置

**修改文件：** `frontend/src/router/index.js`

```javascript
{
  path: '/student/sprint',
  component: () => import('@/views/student/SprintIndex.vue')  // 三合一页面
}
```

### 2. Store

**新建文件：** `frontend/src/store/sprint.js`

**State：**
```javascript
{
  sessionId: null,
  status: 'idle',      // idle | ready | playing | finished
  subjectId: null,
  startTime: null,
  currentQuestion: null,
  combo: 0,
  maxCombo: 0,
  score: 0,
  questionCount: 0,
  correctCount: 0,
  wrongCount: 0,
  timeLeft: 60,
  rewards: { gold: 0, exp: 0, diamond: 0 },
  historyBest: { bestScore: 0, bestCombo: 0, bestAccuracy: 0 },
  result: null
}
```

### 3. API 封装

**修改文件：** `frontend/src/api/index.js`

```javascript
export const sprintApi = {
  start: (data) => api.post('/practice/sprint/start', data),
  getQuestion: (sessionId) => api.get(`/practice/sprint/question/${sessionId}`),
  submitAnswer: (data) => api.post('/practice/sprint/answer', data),
  finish: (data) => api.post('/practice/sprint/finish', data),
  getHistory: (studentId) => api.get(`/practice/sprint/history/${studentId}`),
  getBest: (studentId) => api.get(`/practice/sprint/best/${studentId}`)
}
```

### 4. 页面组件

**新建文件：** `frontend/src/views/student/SprintIndex.vue`

**页面状态流转：**
```
idle → ready → playing → finished
  ↑__________________|__________|
         (再来一局)
```

**子组件结构（内联或拆分）：**
- `SprintStart` - 开始页面（选择科目、历史最佳）
- `SprintPlay` - 答题页面（倒计时、题目、选项）
- `SprintResult` - 结算页面（成绩、奖励、新成就）

### 5. Home.vue 入口调整

**修改文件：** `frontend/src/views/student/Home.vue`

```javascript
const startPractice = (mode) => {
  if (mode === 'sprint') {
    router.push('/student/sprint')
  } else {
    router.push({ path: '/student/practice', query: { mode } })
  }
}
```

---

## 关键文件清单

### 后端新建
| 文件 | 说明 |
|------|------|
| `entity/SprintRecord.java` | 冲刺记录实体 |
| `entity/SprintSession.java` | 冲刺会话实体 |
| `mapper/SprintRecordMapper.java` | 冲刺记录 Mapper |
| `mapper/SprintSessionMapper.java` | 冲刺会话 Mapper |
| `dto/SprintStartRequest.java` | 开始请求 DTO |
| `dto/SprintAnswerRequest.java` | 答题请求 DTO |
| `dto/SprintFinishRequest.java` | 结束请求 DTO |
| `dto/SprintQuestionResponse.java` | 题目响应 DTO |
| `dto/SprintAnswerResponse.java` | 答题响应 DTO |
| `dto/SprintResultResponse.java` | 结算响应 DTO |
| `service/SprintService.java` | 冲刺业务逻辑 |
| `controller/SprintController.java` | 冲刺 API |

### 前端新建
| 文件 | 说明 |
|------|------|
| `store/sprint.js` | 冲刺状态管理 |
| `views/student/SprintIndex.vue` | 冲刺主页面 |

### 后端修改
| 文件 | 修改内容 |
|------|----------|
| `resources/schema.sql` | 追加表结构 |
| `resources/application.yml` | 可选配置 |

### 前端修改
| 文件 | 修改内容 |
|------|----------|
| `router/index.js` | 添加路由 |
| `api/index.js` | 添加 API |
| `views/student/Home.vue` | 修复入口跳转 |

---

## 验证方法

1. **数据库验证**
   - 启动 MySQL，执行 SQL 脚本
   - 验证 `t_sprint_record` 和 `t_sprint_session` 表创建成功

2. **后端验证**
   - 启动后端 `mvn spring-boot:run`
   - 测试 API：
     - `POST /api/practice/sprint/start`
     - `POST /api/practice/sprint/answer`
     - `POST /api/practice/sprint/finish`
     - `GET /api/practice/sprint/history/1`
     - `GET /api/practice/sprint/best/1`

3. **前端验证**
   - 启动前端 `npm run dev`
   - 访问 `/student/sprint`
   - 测试完整流程：选择科目 → 开始冲刺 → 答题 → 结算

4. **功能验证**
   - ✅ 60 秒倒计时正常工作
   - ✅ 答对加分、答错不减分
   - ✅ 连击机制正常（3/5/10/20 连击钻石奖励）
   - ✅ 时间加成正常（30秒/45秒分档）
   - ✅ 结算页面显示正确
   - ✅ 历史记录保存成功
   - ✅ 再来一局功能正常

---

## 实现顺序

1. **数据库**：执行 SQL 创建表
2. **后端实体 + Mapper**：SprintRecord、SprintSession、两个 Mapper
3. **后端 DTO**：请求/响应对象
4. **后端 Service**：核心业务逻辑
5. **后端 Controller**：API 端点
6. **前端 Store**：状态管理
7. **前端 API**：接口封装
8. **前端页面**：SprintIndex.vue
9. **前端路由 + 入口**：router、Home.vue
10. **联调测试**

---

## 简化说明

为加快实现速度，采用以下简化策略：

1. **三页面合一**：将 SprintStart、SprintPlay、SprintResult 合并为一个 SprintIndex.vue，通过状态切换显示不同部分

2. **题目随机获取**：复用现有 `questionApi.getBySubject`，前端自行随机选择和过滤

3. **Session 管理**：使用 localStorage 存储当前 sessionId，支持页面刷新恢复

4. **倒计时**：纯前端实现，使用 setInterval，每秒更新

5. **音效**：暂时跳过，后续迭代添加

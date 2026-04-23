# 冲刺模式知识点掌握度逻辑实现

## Context

冲刺模式（Sprint Mode）中答对题目时应该：
1. 更新对应知识点的掌握度
2. 出题时优先选择掌握度低的知识点对应的题目

目前 `StudentKnowledgeMastery` 实体已存在，`QuestionService` 中有 `updateMastery()` 方法，但 `SprintService.submitAnswer()` 答对时没有调用它，且选题使用纯随机 `ORDER BY RAND()`。

## 实现方案

### 1. 创建 KnowledgeMasteryService（提取并增强现有逻辑）

**新建文件:** `src/main/java/com/studyagent/service/KnowledgeMasteryService.java`

从 `QuestionService.updateMastery()` 提取逻辑并增强：
- `updateMastery(Long studentId, Long kpId, boolean correct)` - 更新知识点掌握度
- `getMasteryLevel(Long studentId, Long kpId)` - 获取单个知识点掌握度
- `getStudentMasteryMap(Long studentId, Long subjectId)` - 获取学生在某科目下所有知识点的掌握度 Map

### 2. 修改 SprintService.submitAnswer() - 答对时更新掌握度

**文件:** `src/main/java/com/studyagent/service/SprintService.java`

在答对分支（第 106-126 行）添加：
```java
// 更新知识点掌握度
if (question.getKnowledgePointIds() != null && !question.getKnowledgePointIds().isEmpty()) {
    for (String kpIdStr : question.getKnowledgePointIds().split(",")) {
        try {
            Long kpId = Long.parseLong(kpIdStr.trim());
            knowledgeMasteryService.updateMastery(studentId, kpId, true);
        } catch (NumberFormatException e) {
            log.warn("解析知识点ID失败: {}", kpIdStr);
        }
    }
}
```

### 3. 修改选题逻辑 - 根据掌握度加权

**文件:** `src/main/java/com/studyagent/service/SprintService.java`

修改 `getRandomQuestion()` 方法：

```java
private Question getRandomQuestion(SprintSession session) {
    List<Long> usedIds = parseUsedQuestionIds(session.getUsedQuestionIds());

    // 获取学生对该科目各知识点的掌握度
    Map<Long, Double> masteryMap = knowledgeMasteryService
        .getStudentMasteryMap(session.getStudentId(), session.getSubjectId());

    // 获取该科目所有可用题目
    List<Question> questions = questionMapper.selectBySubjectId(session.getSubjectId());
    questions = questions.stream()
        .filter(q -> !usedIds.contains(q.getId()))
        .filter(q -> q.getDel() == 0)
        .collect(Collectors.toList());

    if (questions.isEmpty()) {
        return null;
    }

    // 根据知识点掌握度加权选择：掌握度越低权重越高
    // 权重 = 1.0 - masteryLevel（掌握度 0.9 的题目权重为 0.1）
    List<Double> weights = questions.stream()
        .map(q -> calculateQuestionWeight(q, masteryMap))
        .collect(Collectors.toList());

    // 加权随机选择
    return weightedRandomSelect(questions, weights);
}

private double calculateQuestionWeight(Question q, Map<Long, Double> masteryMap) {
    if (q.getKnowledgePointIds() == null || q.getKnowledgePointIds().isEmpty()) {
        return 0.5; // 无知识点标记的题目使用中间权重
    }

    double minMastery = 1.0;
    for (String kpIdStr : q.getKnowledgePointIds().split(",")) {
        try {
            Long kpId = Long.parseLong(kpIdStr.trim());
            Double mastery = masteryMap.getOrDefault(kpId, 0.0);
            if (mastery < minMastery) {
                minMastery = mastery;
            }
        } catch (NumberFormatException e) {
            // 忽略无效ID
        }
    }
    // 掌握度越低，权重越高（确保完全掌握的题目很少被选）
    return Math.max(0.05, 1.0 - minMastery);
}

private Question weightedRandomSelect(List<Question> questions, List<Double> weights) {
    double totalWeight = weights.stream().mapToDouble(Double::doubleValue).sum();
    double random = Math.random() * totalWeight;
    double cumulative = 0;
    for (int i = 0; i < questions.size(); i++) {
        cumulative += weights.get(i);
        if (random <= cumulative) {
            return questions.get(i);
        }
    }
    return questions.get(questions.size() - 1);
}
```

### 4. 修改 QuestionMapper - 添加 selectBySubjectId

**文件:** `src/main/java/com/studyagent/mapper/QuestionMapper.java`

```java
@Select("SELECT * FROM t_question WHERE subject_id = #{subjectId} AND del = 0 ORDER BY id")
List<Question> selectBySubjectId(@Param("subjectId") Long subjectId);
```

### 5. 添加依赖注入

**文件:** `src/main/java/com/studyagent/service/SprintService.java`

```java
private final KnowledgeMasteryService knowledgeMasteryService;
```

## 关键文件

| 文件 | 操作 |
|------|------|
| `src/main/java/com/studyagent/service/KnowledgeMasteryService.java` | 新建 |
| `src/main/java/com/studyagent/service/SprintService.java` | 修改 |
| `src/main/java/com/studyagent/mapper/QuestionMapper.java` | 修改 |

## 验证方案

1. **运行现有 E2E 测试** - 确保冲刺流程仍正常工作：
   ```bash
   cd frontend && npx playwright test tests/e2e/sprint.spec.js
   ```

2. **手动验证知识点更新**：
   - 完成冲刺并答对一些题
   - 调用 `GET /api/mastery/student/{studentId}/subject/{subjectId}` 检查掌握度变化

3. **验证选题逻辑**：
   - 多次开始冲刺，验证低掌握度知识点对应的题目被优先选中

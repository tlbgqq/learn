# AI 生成题目方案

## Context

需要为 `t_knowledge_point` 表中的每个知识点自动生成 5 道练习题，用于题库填充。现有项目中 AI 仅用于错题分析（QuestionAnalysisService），需要新增题目生成功能。

## 现有资源

- **ChatModel**: `LangChain4jConfig` 中配置的 OpenAiChatModel，provider=minimax，temperature=0.7，maxTokens=50000
- **QuestionService.create()**: 已有创建题目的方法，参数包括 type, content, answer, analysis, subjectId, knowledgePointIds, difficulty
- **QuestionMapper**: 已有的 MyBatis Plus Mapper

## 方案设计

### 1. 新建 QuestionGenerationService

位置：`ai/QuestionGenerationService.java`

```java
@Service
public class QuestionGenerationService {
    private final ChatModel chatModel;
    private final QuestionService questionService;

    // 核心方法：为单个知识点生成5道题
    public List<Question> generateForKnowledgePoint(KnowledgePoint kp) {
        // 1. 构建 Prompt（包含知识点描述、难度、题目类型要求）
        // 2. 调用 chatModel.chat()
        // 3. 解析返回的 JSON（5道题目的数组）
        // 4. 调用 questionService.create() 保存每道题
        // 5. 返回生成的题目列表
    }

    // 批量生成（扫描所有知识点）
    public int generateForAll() { ... }
}
```

### 2. AI Prompt 设计

```text
你是一个K12教育题目生成专家。请根据以下知识点信息，生成5道练习题。

知识点信息：
- 名称：{name}
- 描述：{description}
- 难度等级：{difficulty}（1-5星，1为最简单）
- 常见错误：{commonErrors}
- 关联概念：{relatedConcepts}

题目要求：
1. 选择题2道、填空题2道、解答题1道
2. 难度与知识点难度一致
3. 答案准确，解析清晰

请以JSON数组格式返回，题目结构：
[
  {"type": "选择", "content": "题干", "options": ["A. 选项1","B. 选项2","C. 选项3","D. 选项4"], "answer": "B", "analysis": "解析"},
  {"type": "填空", "content": "题干，缺少____", "options": [], "answer": "答案", "analysis": "解析"},
  ...
]
```

### 3. JSON 解析策略

参考 `QuestionAnalysisService.parseAnalysisResult()` 的正则提取方式：
```java
private List<GeneratedQuestion> parseQuestions(String response) {
    String json = extractJson(response);  // 提取 { } 之间的内容
    // 使用正则提取每个题目的字段
}
```

### 4. 错误处理

- AI 调用失败：记录日志，返回空列表，跳过该知识点
- JSON 解析失败：使用 fallback 兜底，返回空列表
- 题目保存失败：事务回滚，记录错误

### 5. API 接口设计

**QuestionGenerationController** (可选):

```
POST /api/question/generate/{kpId}     - 为单个知识点生成
POST /api/question/generate/batch      - 批量生成所有知识点
GET  /api/question/generate/status     - 查看生成状态
```

### 6. 关键文件路径

| 操作 | 文件路径 |
|------|---------|
| 新建 | `ai/QuestionGenerationService.java` |
| 新建 | `controller/QuestionGenerationController.java` (可选) |
| 修改 | 无需修改现有实体/Mapper |

### 7. 验证方式

1. 调用 `POST /api/question/generate/1` 测试单个知识点
2. 查询 `t_question` 表验证题目已入库
3. 检查题目与知识点的关联（knowledge_point_ids 字段）

## 风险与注意事项

1. **API 配额**：批量生成所有知识点可能产生大量 API 调用，考虑添加限流或分批处理
2. **去重**：如果知识点已有题目，需要判断是否跳过或覆盖
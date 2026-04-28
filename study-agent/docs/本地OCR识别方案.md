# 本地OCR识别方案

## 架构

```
用户上传图片 → Tesseract OCR 识别文字 → MiniMax AI 整理成结构化JSON
```

## 技术选型

| 组件 | 选型 | 说明 |
|------|------|------|
| OCR引擎 | Tesseract 5.7.0 | 开源免费，支持中文识别 |
| 语言包 | chi_sim+eng | 中文简体+英文混合识别 |
| AI整理 | MiniMax-M2.7 | 将OCR结果转成结构化JSON |

## 文件清单

| 文件 | 说明 |
|------|------|
| `OcrService.java` | OCR文字识别服务 |
| `ExamParseResult.java` | 结构化结果DTO |
| `ImageParsingTestController.java` | 测试接口 |

## OCR配置

需要设置环境变量 `TESSDATA_PREFIX` 指向 tessdata 目录。

中文语言包下载：https://github.com/tesseract-ocr/tessdata

## API 接口

```
POST /api/test/image-parse
Content-Type: multipart/form-data

请求参数：
- image: 文件（图片）

响应：
{
  "success": true,
  "ocrText": "识别的文字内容...",
  "examResult": {
    "studentName": "张三",
    "studentId": "2024001",
    "teacherName": "李老师",
    "questions": [...]
  }
}
```

## 依赖

```xml
<dependency>
    <groupId>net.sourceforge.tess4j</groupId>
    <artifactId>tess4j</artifactId>
    <version>5.7.0</version>
</dependency>
```

## 注意事项

1. Tesseract 需要本地安装并配置 tessdata 目录
2. 图片质量影响识别率，建议先做图像预处理（二值化、去噪）
3. 对于复杂试卷可能需要人工校验OCR结果
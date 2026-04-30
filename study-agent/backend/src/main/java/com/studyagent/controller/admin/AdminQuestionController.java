package com.studyagent.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyagent.dto.ApiResponse;
import com.studyagent.entity.KnowledgePoint;
import com.studyagent.entity.Question;
import com.studyagent.entity.Subject;
import com.studyagent.mapper.KnowledgePointMapper;
import com.studyagent.mapper.QuestionMapper;
import com.studyagent.mapper.SubjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/question")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
public class AdminQuestionController {

    private final QuestionMapper questionMapper;
    private final SubjectMapper subjectMapper;
    private final KnowledgePointMapper knowledgePointMapper;

    @GetMapping("/list")
    public ApiResponse<Page<QuestionVO>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String knowledgePointIds,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        
        if (subjectId != null) {
            wrapper.eq(Question::getSubjectId, subjectId);
        }
        if (StringUtils.hasText(knowledgePointIds)) {
            String[] ids = knowledgePointIds.split(",");
            wrapper.and(w -> {
                for (String id : ids) {
                    w.or().like(Question::getKnowledgePointIds, id);
                }
            });
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(Question::getType, type);
        }
        if (difficulty != null) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Question::getContent, keyword);
        }
        
        wrapper.orderByDesc(Question::getId);
        
        Page<Question> page = questionMapper.selectPage(new Page<>(current, size), wrapper);
        
        List<QuestionVO> voList = convertToVO(page.getRecords());
        
        Page<QuestionVO> voPage = new Page<>();
        voPage.setCurrent(page.getCurrent());
        voPage.setSize(page.getSize());
        voPage.setTotal(page.getTotal());
        voPage.setRecords(voList);
        
        return ApiResponse.success(voPage);
    }

    @GetMapping("/{id}")
    public ApiResponse<QuestionVO> getById(@PathVariable Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            return ApiResponse.error("题目不存在");
        }
        return ApiResponse.success(convertToVO(question));
    }

    @PostMapping
    public ApiResponse<String> add(@RequestBody Question question) {
        try {
            question.setFrequency(0);
            question.setCreateTime(LocalDateTime.now());
            question.setModifyTime(LocalDateTime.now());
            questionMapper.insert(question);
            return ApiResponse.success("新增成功", null);
        } catch (Exception e) {
            log.error("新增题目失败", e);
            return ApiResponse.error("新增失败：" + e.getMessage());
        }
    }

    @PutMapping
    public ApiResponse<String> update(@RequestBody Question question) {
        try {
            Question existing = questionMapper.selectById(question.getId());
            if (existing == null) {
                return ApiResponse.error("题目不存在");
            }
            question.setModifyTime(LocalDateTime.now());
            question.setCreateTime(existing.getCreateTime());
            question.setFrequency(existing.getFrequency());
            questionMapper.updateById(question);
            return ApiResponse.success("更新成功", null);
        } catch (Exception e) {
            log.error("更新题目失败", e);
            return ApiResponse.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        try {
            Question question = questionMapper.selectById(id);
            if (question == null) {
                return ApiResponse.error("题目不存在");
            }
            questionMapper.deleteById(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            log.error("删除题目失败", e);
            return ApiResponse.error("删除失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/batch")
    public ApiResponse<String> batchDelete(@RequestBody Map<String, List<Long>> params) {
        try {
            List<Long> ids = params.get("ids");
            if (ids == null || ids.isEmpty()) {
                return ApiResponse.error("请选择要删除的题目");
            }
            questionMapper.deleteBatchIds(ids);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            log.error("批量删除题目失败", e);
            return ApiResponse.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/parents")
    public ApiResponse<List<QuestionVO>> getParentQuestions(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long excludeId) {
        try {
            LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Question::getParentId, 0L);
            
            if (subjectId != null && subjectId > 0) {
                wrapper.eq(Question::getSubjectId, subjectId);
            }
            if (excludeId != null && excludeId > 0) {
                wrapper.ne(Question::getId, excludeId);
            }
            
            wrapper.orderByDesc(Question::getId);
            List<Question> questions = questionMapper.selectList(wrapper);
            
            return ApiResponse.success(convertToVO(questions));
        } catch (Exception e) {
            log.error("获取父题目列表失败", e);
            return ApiResponse.error("获取失败：" + e.getMessage());
        }
    }

    @GetMapping("/children/{parentId}")
    public ApiResponse<List<QuestionVO>> getChildrenQuestions(@PathVariable Long parentId) {
        try {
            if (parentId == null || parentId <= 0) {
                return ApiResponse.error("父题目ID不能为空");
            }
            
            LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Question::getParentId, parentId);
            wrapper.orderByAsc(Question::getId);
            List<Question> questions = questionMapper.selectList(wrapper);
            
            return ApiResponse.success(convertToVO(questions));
        } catch (Exception e) {
            log.error("获取子题目列表失败", e);
            return ApiResponse.error("获取失败：" + e.getMessage());
        }
    }

    @PostMapping("/import")
    public ApiResponse<ImportResultVO> importQuestions(@RequestParam("file") MultipartFile file) {
        try {
            List<QuestionImportVO> importList = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    QuestionImportVO vo = parseRow(row);
                    importList.add(vo);
                } catch (Exception e) {
                    errors.add("第" + (i + 1) + "行：" + e.getMessage());
                }
            }
            
            workbook.close();
            
            ImportResultVO result = new ImportResultVO();
            result.setTotal(importList.size());
            result.setErrors(errors);
            
            int successCount = 0;
            for (QuestionImportVO vo : importList) {
                try {
                    Question question = convertToEntity(vo);
                    questionMapper.insert(question);
                    successCount++;
                } catch (Exception e) {
                    log.error("导入题目失败", e);
                }
            }
            result.setSuccessCount(successCount);
            
            return ApiResponse.success("导入完成", result);
        } catch (Exception e) {
            log.error("导入题目失败", e);
            return ApiResponse.error("导入失败：" + e.getMessage());
        }
    }

    @GetMapping("/export")
    public void exportQuestions(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String knowledgePointIds,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String keyword,
            HttpServletResponse response) {
        
        try {
            LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
            
            if (subjectId != null) {
                wrapper.eq(Question::getSubjectId, subjectId);
            }
            if (StringUtils.hasText(knowledgePointIds)) {
                String[] ids = knowledgePointIds.split(",");
                wrapper.and(w -> {
                    for (String id : ids) {
                        w.or().like(Question::getKnowledgePointIds, id);
                    }
                });
            }
            if (StringUtils.hasText(type)) {
                wrapper.eq(Question::getType, type);
            }
            if (difficulty != null) {
                wrapper.eq(Question::getDifficulty, difficulty);
            }
            if (StringUtils.hasText(keyword)) {
                wrapper.like(Question::getContent, keyword);
            }
            
            wrapper.orderByDesc(Question::getId);
            wrapper.last("LIMIT 10000");
            
            List<Question> questions = questionMapper.selectList(wrapper);
            List<QuestionVO> voList = convertToVO(questions);
            
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("题目列表");
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "题目类型", "题目内容", "选项", "正确答案", "解析", "学科", "知识点", "难度", "出题频率", "创建时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (int i = 0; i < voList.size(); i++) {
                QuestionVO vo = voList.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(vo.getId());
                row.createCell(1).setCellValue(vo.getType());
                row.createCell(2).setCellValue(vo.getContent());
                row.createCell(3).setCellValue(vo.getOptions());
                row.createCell(4).setCellValue(vo.getAnswer());
                row.createCell(5).setCellValue(vo.getAnalysis());
                row.createCell(6).setCellValue(vo.getSubjectName());
                row.createCell(7).setCellValue(vo.getKnowledgePointNames());
                row.createCell(8).setCellValue(vo.getDifficulty());
                row.createCell(9).setCellValue(vo.getFrequency());
                row.createCell(10).setCellValue(vo.getCreateTime() != null ? vo.getCreateTime().format(formatter) : "");
            }
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("题目列表_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            
            workbook.write(response.getOutputStream());
            workbook.close();
            
        } catch (Exception e) {
            log.error("导出题目失败", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "导出失败");
            } catch (IOException ex) {
                log.error("发送错误响应失败", ex);
            }
        }
    }

    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("导入模板");
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {"type", "content", "options", "answer", "analysis", "subject_id", "knowledge_point_ids", "difficulty"};
            String[] comments = {"题目类型：选择/填空/解答", "题目内容", "选项（选择题填写，每行一个）", "正确答案", "解析", "学科ID", "知识点ID（逗号分隔）", "难度(1-5)"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("选择");
            exampleRow.createCell(1).setCellValue("1+1等于几？");
            exampleRow.createCell(2).setCellValue("A. 1\nB. 2\nC. 3\nD. 4");
            exampleRow.createCell(3).setCellValue("B");
            exampleRow.createCell(4).setCellValue("1+1=2，基础加法运算");
            exampleRow.createCell(5).setCellValue("1");
            exampleRow.createCell(6).setCellValue("1,2");
            exampleRow.createCell(7).setCellValue("1");
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("题目导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            
            workbook.write(response.getOutputStream());
            workbook.close();
            
        } catch (Exception e) {
            log.error("下载模板失败", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "下载模板失败");
            } catch (IOException ex) {
                log.error("发送错误响应失败", ex);
            }
        }
    }

    private List<QuestionVO> convertToVO(List<Question> questions) {
        List<QuestionVO> voList = new ArrayList<>();
        for (Question q : questions) {
            voList.add(convertToVO(q));
        }
        return voList;
    }

    private QuestionVO convertToVO(Question question) {
        QuestionVO vo = new QuestionVO();
        vo.setId(question.getId());
        vo.setType(question.getType());
        vo.setContent(question.getContent());
        vo.setOptions(question.getOptions());
        vo.setAnswer(question.getAnswer());
        vo.setAnalysis(question.getAnalysis());
        vo.setSubjectId(question.getSubjectId());
        vo.setKnowledgePointIds(question.getKnowledgePointIds());
        vo.setDifficulty(question.getDifficulty());
        vo.setFrequency(question.getFrequency());
        vo.setParentId(question.getParentId());
        vo.setCreateTime(question.getCreateTime());
        vo.setModifyTime(question.getModifyTime());
        
        if (question.getSubjectId() != null && question.getSubjectId() > 0) {
            Subject subject = subjectMapper.selectById(question.getSubjectId());
            if (subject != null) {
                vo.setSubjectName(subject.getName());
            }
        }
        
        if (StringUtils.hasText(question.getKnowledgePointIds())) {
            List<String> kpNames = new ArrayList<>();
            String[] kpIds = question.getKnowledgePointIds().split(",");
            for (String kpId : kpIds) {
                try {
                    KnowledgePoint kp = knowledgePointMapper.selectById(Long.parseLong(kpId.trim()));
                    if (kp != null) {
                        kpNames.add(kp.getName());
                    }
                } catch (NumberFormatException e) {
                    log.warn("解析知识点ID失败: {}", kpId);
                }
            }
            vo.setKnowledgePointNames(String.join(", ", kpNames));
        }
        
        return vo;
    }

    private QuestionImportVO parseRow(Row row) {
        QuestionImportVO vo = new QuestionImportVO();
        
        Cell typeCell = row.getCell(0);
        if (typeCell == null || !StringUtils.hasText(getCellValue(typeCell))) {
            throw new RuntimeException("题目类型不能为空");
        }
        vo.setType(getCellValue(typeCell));
        
        Cell contentCell = row.getCell(1);
        if (contentCell == null || !StringUtils.hasText(getCellValue(contentCell))) {
            throw new RuntimeException("题目内容不能为空");
        }
        vo.setContent(getCellValue(contentCell));
        
        Cell optionsCell = row.getCell(2);
        if (optionsCell != null) {
            vo.setOptions(getCellValue(optionsCell));
        }
        
        Cell answerCell = row.getCell(3);
        if (answerCell == null || !StringUtils.hasText(getCellValue(answerCell))) {
            throw new RuntimeException("正确答案不能为空");
        }
        vo.setAnswer(getCellValue(answerCell));
        
        Cell analysisCell = row.getCell(4);
        if (analysisCell != null) {
            vo.setAnalysis(getCellValue(analysisCell));
        }
        
        Cell subjectIdCell = row.getCell(5);
        if (subjectIdCell == null) {
            throw new RuntimeException("学科ID不能为空");
        }
        vo.setSubjectId(Long.parseLong(getCellValue(subjectIdCell)));
        
        Cell kpIdsCell = row.getCell(6);
        if (kpIdsCell != null) {
            vo.setKnowledgePointIds(getCellValue(kpIdsCell));
        }
        
        Cell difficultyCell = row.getCell(7);
        if (difficultyCell != null) {
            try {
                vo.setDifficulty(Integer.parseInt(getCellValue(difficultyCell)));
            } catch (NumberFormatException e) {
                vo.setDifficulty(1);
            }
        } else {
            vo.setDifficulty(1);
        }
        
        return vo;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    private Question convertToEntity(QuestionImportVO vo) {
        Question question = new Question();
        question.setType(vo.getType());
        question.setContent(vo.getContent());
        question.setOptions(vo.getOptions() != null ? vo.getOptions() : "");
        question.setAnswer(vo.getAnswer());
        question.setAnalysis(vo.getAnalysis() != null ? vo.getAnalysis() : "");
        question.setSubjectId(vo.getSubjectId());
        question.setKnowledgePointIds(vo.getKnowledgePointIds() != null ? vo.getKnowledgePointIds() : "");
        question.setDifficulty(vo.getDifficulty());
        question.setFrequency(0);
        question.setCreateTime(LocalDateTime.now());
        question.setModifyTime(LocalDateTime.now());
        return question;
    }

    @lombok.Data
    public static class QuestionVO {
        private Long id;
        private String type;
        private String content;
        private String options;
        private String answer;
        private String analysis;
        private Long subjectId;
        private String subjectName;
        private String knowledgePointIds;
        private String knowledgePointNames;
        private Integer difficulty;
        private Integer frequency;
        private Long parentId;
        private LocalDateTime createTime;
        private LocalDateTime modifyTime;
    }

    @lombok.Data
    public static class QuestionImportVO {
        private String type;
        private String content;
        private String options;
        private String answer;
        private String analysis;
        private Long subjectId;
        private String knowledgePointIds;
        private Integer difficulty;
    }

    @lombok.Data
    public static class ImportResultVO {
        private Integer total;
        private Integer successCount;
        private List<String> errors;
    }
}

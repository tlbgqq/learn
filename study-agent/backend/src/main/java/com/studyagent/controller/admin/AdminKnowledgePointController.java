package com.studyagent.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyagent.dto.ApiResponse;
import com.studyagent.entity.KnowledgePoint;
import com.studyagent.entity.Subject;
import com.studyagent.mapper.KnowledgePointMapper;
import com.studyagent.mapper.SubjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/knowledge-point")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
public class AdminKnowledgePointController {

    private final KnowledgePointMapper knowledgePointMapper;
    private final SubjectMapper subjectMapper;

    @GetMapping("/tree")
    public ApiResponse<List<KnowledgePointTreeVO>> getTree(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long gradeId) {
        
        LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<>();
        
        if (subjectId != null) {
            wrapper.eq(KnowledgePoint::getSubjectId, subjectId);
        }
        if (gradeId != null) {
            wrapper.eq(KnowledgePoint::getGradeId, gradeId);
        }
        
        wrapper.orderByAsc(KnowledgePoint::getSortOrder);
        
        List<KnowledgePoint> allPoints = knowledgePointMapper.selectList(wrapper);
        
        List<KnowledgePointTreeVO> tree = buildTree(allPoints);
        
        return ApiResponse.success(tree);
    }

    @GetMapping("/list")
    public ApiResponse<Page<KnowledgePointVO>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) String keyword) {
        
        LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<>();
        
        if (subjectId != null) {
            wrapper.eq(KnowledgePoint::getSubjectId, subjectId);
        }
        if (gradeId != null) {
            wrapper.eq(KnowledgePoint::getGradeId, gradeId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(KnowledgePoint::getName, keyword)
                    .or().like(KnowledgePoint::getCode, keyword)
                    .or().like(KnowledgePoint::getDescription, keyword)
            );
        }
        
        wrapper.orderByAsc(KnowledgePoint::getSortOrder);
        
        Page<KnowledgePoint> page = knowledgePointMapper.selectPage(new Page<>(current, size), wrapper);
        
        List<KnowledgePointVO> voList = convertToVO(page.getRecords());
        
        Page<KnowledgePointVO> voPage = new Page<>();
        voPage.setCurrent(page.getCurrent());
        voPage.setSize(page.getSize());
        voPage.setTotal(page.getTotal());
        voPage.setRecords(voList);
        
        return ApiResponse.success(voPage);
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgePointVO> getById(@PathVariable Long id) {
        KnowledgePoint kp = knowledgePointMapper.selectById(id);
        if (kp == null) {
            return ApiResponse.error("知识点不存在");
        }
        return ApiResponse.success(convertToVO(kp));
    }

    @PostMapping
    public ApiResponse<String> add(@RequestBody KnowledgePoint kp) {
        try {
            if (kp.getParentId() == null) {
                kp.setParentId(0L);
            }
            if (kp.getSortOrder() == null) {
                kp.setSortOrder(0);
            }
            if (kp.getDifficulty() == null) {
                kp.setDifficulty(1);
            }
            kp.setCreateTime(LocalDateTime.now());
            kp.setModifyTime(LocalDateTime.now());
            knowledgePointMapper.insert(kp);
            return ApiResponse.success("新增成功", null);
        } catch (Exception e) {
            log.error("新增知识点失败", e);
            return ApiResponse.error("新增失败：" + e.getMessage());
        }
    }

    @PutMapping
    public ApiResponse<String> update(@RequestBody KnowledgePoint kp) {
        try {
            KnowledgePoint existing = knowledgePointMapper.selectById(kp.getId());
            if (existing == null) {
                return ApiResponse.error("知识点不存在");
            }
            kp.setModifyTime(LocalDateTime.now());
            kp.setCreateTime(existing.getCreateTime());
            knowledgePointMapper.updateById(kp);
            return ApiResponse.success("更新成功", null);
        } catch (Exception e) {
            log.error("更新知识点失败", e);
            return ApiResponse.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        try {
            KnowledgePoint kp = knowledgePointMapper.selectById(id);
            if (kp == null) {
                return ApiResponse.error("知识点不存在");
            }
            
            List<KnowledgePoint> children = knowledgePointMapper.selectList(
                    new LambdaQueryWrapper<KnowledgePoint>()
                            .eq(KnowledgePoint::getParentId, id)
            );
            if (!children.isEmpty()) {
                return ApiResponse.error("该知识点下有子知识点，无法删除");
            }
            
            knowledgePointMapper.deleteById(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            log.error("删除知识点失败", e);
            return ApiResponse.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/subjects")
    public ApiResponse<List<Subject>> getSubjects(
            @RequestParam(required = false) Long gradeId) {
        
        LambdaQueryWrapper<Subject> wrapper = new LambdaQueryWrapper<>();
        
        if (gradeId != null) {
            wrapper.eq(Subject::getGradeId, gradeId);
        }
        
        wrapper.orderByAsc(Subject::getSortOrder);
        
        List<Subject> subjects = subjectMapper.selectList(wrapper);
        
        return ApiResponse.success(subjects);
    }

    private List<KnowledgePointTreeVO> buildTree(List<KnowledgePoint> allPoints) {
        Map<Long, KnowledgePointTreeVO> voMap = new HashMap<>();
        List<KnowledgePointTreeVO> roots = new ArrayList<>();
        
        for (KnowledgePoint kp : allPoints) {
            KnowledgePointTreeVO vo = convertToTreeVO(kp);
            voMap.put(kp.getId(), vo);
        }
        
        for (KnowledgePoint kp : allPoints) {
            KnowledgePointTreeVO vo = voMap.get(kp.getId());
            if (kp.getParentId() == null || kp.getParentId() == 0) {
                roots.add(vo);
            } else {
                KnowledgePointTreeVO parent = voMap.get(kp.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(vo);
                } else {
                    roots.add(vo);
                }
            }
        }
        
        return roots;
    }

    private List<KnowledgePointVO> convertToVO(List<KnowledgePoint> kps) {
        List<KnowledgePointVO> voList = new ArrayList<>();
        for (KnowledgePoint kp : kps) {
            voList.add(convertToVO(kp));
        }
        return voList;
    }

    private KnowledgePointVO convertToVO(KnowledgePoint kp) {
        KnowledgePointVO vo = new KnowledgePointVO();
        vo.setId(kp.getId());
        vo.setName(kp.getName());
        vo.setCode(kp.getCode());
        vo.setParentId(kp.getParentId());
        vo.setSubjectId(kp.getSubjectId());
        vo.setGradeId(kp.getGradeId());
        vo.setDifficulty(kp.getDifficulty());
        vo.setDescription(kp.getDescription());
        vo.setCommonErrors(kp.getCommonErrors());
        vo.setRelatedConcepts(kp.getRelatedConcepts());
        vo.setSortOrder(kp.getSortOrder());
        vo.setCreateTime(kp.getCreateTime());
        vo.setModifyTime(kp.getModifyTime());
        
        if (kp.getSubjectId() != null && kp.getSubjectId() > 0) {
            Subject subject = subjectMapper.selectById(kp.getSubjectId());
            if (subject != null) {
                vo.setSubjectName(subject.getName());
            }
        }
        
        if (kp.getParentId() != null && kp.getParentId() > 0) {
            KnowledgePoint parent = knowledgePointMapper.selectById(kp.getParentId());
            if (parent != null) {
                vo.setParentName(parent.getName());
            }
        }
        
        return vo;
    }

    private KnowledgePointTreeVO convertToTreeVO(KnowledgePoint kp) {
        KnowledgePointTreeVO vo = new KnowledgePointTreeVO();
        vo.setId(kp.getId());
        vo.setLabel(kp.getName());
        vo.setCode(kp.getCode());
        vo.setParentId(kp.getParentId());
        vo.setSubjectId(kp.getSubjectId());
        vo.setGradeId(kp.getGradeId());
        vo.setDifficulty(kp.getDifficulty());
        vo.setDescription(kp.getDescription());
        vo.setSortOrder(kp.getSortOrder());
        return vo;
    }

    @lombok.Data
    public static class KnowledgePointVO {
        private Long id;
        private String name;
        private String code;
        private Long parentId;
        private String parentName;
        private Long subjectId;
        private String subjectName;
        private Long gradeId;
        private Integer difficulty;
        private String description;
        private String commonErrors;
        private String relatedConcepts;
        private Integer sortOrder;
        private LocalDateTime createTime;
        private LocalDateTime modifyTime;
    }

    @lombok.Data
    public static class KnowledgePointTreeVO {
        private Long id;
        private String label;
        private String code;
        private Long parentId;
        private Long subjectId;
        private Long gradeId;
        private Integer difficulty;
        private String description;
        private Integer sortOrder;
        private List<KnowledgePointTreeVO> children;
    }
}

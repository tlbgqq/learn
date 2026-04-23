package com.studyagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyagent.entity.KnowledgePoint;
import com.studyagent.entity.Subject;
import com.studyagent.mapper.KnowledgePointMapper;
import com.studyagent.mapper.SubjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgePointService {

    private final KnowledgePointMapper knowledgePointMapper;
    private final SubjectMapper subjectMapper;

    public KnowledgePoint create(String name, String code, Long parentId, Long subjectId,
                                  Long gradeId, Integer difficulty, String description) {
        KnowledgePoint kp = new KnowledgePoint();
        kp.setName(name);
        kp.setCode(code);
        kp.setParentId(parentId);
        kp.setSubjectId(subjectId);
        kp.setGradeId(gradeId);
        kp.setDifficulty(difficulty);
        kp.setDescription(description);
        kp.setSortOrder(0);
        knowledgePointMapper.insert(kp);
        return kp;
    }

    public KnowledgePoint findById(Long id) {
        return knowledgePointMapper.selectById(id);
    }

    public List<KnowledgePoint> findBySubjectAndGrade(Long subjectId, Long gradeId) {
        return knowledgePointMapper.selectList(
                new LambdaQueryWrapper<KnowledgePoint>()
                        .eq(KnowledgePoint::getSubjectId, subjectId)
                        .eq(KnowledgePoint::getGradeId, gradeId)
                        .orderByAsc(KnowledgePoint::getSortOrder)
        );
    }

    public List<KnowledgePoint> findRootBySubject(Long subjectId) {
        return knowledgePointMapper.selectList(
                new LambdaQueryWrapper<KnowledgePoint>()
                        .eq(KnowledgePoint::getSubjectId, subjectId)
                        .eq(KnowledgePoint::getParentId, 0L)
                        .orderByAsc(KnowledgePoint::getSortOrder)
        );
    }

    public List<KnowledgePoint> findChildren(Long parentId) {
        return knowledgePointMapper.selectList(
                new LambdaQueryWrapper<KnowledgePoint>()
                        .eq(KnowledgePoint::getParentId, parentId)
                        .orderByAsc(KnowledgePoint::getSortOrder)
        );
    }

    public KnowledgePoint update(Long id, String name, Integer difficulty, String description) {
        KnowledgePoint kp = knowledgePointMapper.selectById(id);
        if (kp != null) {
            kp.setName(name);
            kp.setDifficulty(difficulty);
            kp.setDescription(description);
            knowledgePointMapper.updateById(kp);
        }
        return kp;
    }

    public void delete(Long id) {
        knowledgePointMapper.deleteById(id);
    }

    public List<Subject> findSubjectsByGrade(Long gradeId) {
        return subjectMapper.selectList(
                new LambdaQueryWrapper<Subject>()
                        .eq(Subject::getGradeId, gradeId)
                        .orderByAsc(Subject::getSortOrder)
        );
    }
}

package com.studyagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyagent.entity.StudentKnowledgeMastery;
import com.studyagent.mapper.KnowledgePointMapper;
import com.studyagent.mapper.StudentKnowledgeMasteryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeMasteryService {

    private final StudentKnowledgeMasteryMapper masteryMapper;
    private final KnowledgePointMapper knowledgePointMapper;

    /**
     * 更新知识点掌握度
     */
    public void updateMastery(Long studentId, Long kpId, boolean correct) {
        StudentKnowledgeMastery mastery = masteryMapper.selectOne(
                new LambdaQueryWrapper<StudentKnowledgeMastery>()
                        .eq(StudentKnowledgeMastery::getStudentId, studentId)
                        .eq(StudentKnowledgeMastery::getKnowledgePointId, kpId)
        );

        if (mastery == null) {
            mastery = new StudentKnowledgeMastery();
            mastery.setStudentId(studentId);
            mastery.setKnowledgePointId(kpId);
            mastery.setMasteryLevel(0.0);
            mastery.setStarLevel(0);
            mastery.setPracticeCount(0);
            mastery.setCorrectCount(0);
        }

        mastery.setPracticeCount(mastery.getPracticeCount() + 1);
        if (correct) {
            mastery.setCorrectCount(mastery.getCorrectCount() + 1);
        }

        double rate = mastery.getPracticeCount() > 0
                ? (double) mastery.getCorrectCount() / mastery.getPracticeCount()
                : 0;
        mastery.setMasteryLevel(rate);
        mastery.setStarLevel(calculateStarLevel(rate));

        if (mastery.getId() == null) {
            masteryMapper.insert(mastery);
        } else {
            masteryMapper.updateById(mastery);
        }
    }

    /**
     * 获取单个知识点的掌握度
     */
    public Double getMasteryLevel(Long studentId, Long kpId) {
        StudentKnowledgeMastery mastery = masteryMapper.selectOne(
                new LambdaQueryWrapper<StudentKnowledgeMastery>()
                        .eq(StudentKnowledgeMastery::getStudentId, studentId)
                        .eq(StudentKnowledgeMastery::getKnowledgePointId, kpId)
        );
        return mastery != null ? mastery.getMasteryLevel() : 0.0;
    }

    /**
     * 获取学生在某科目下所有知识点的掌握度 Map
     * key: knowledgePointId, value: masteryLevel
     */
    public Map<Long, Double> getStudentMasteryMap(Long studentId, Long subjectId) {
        // 获取该科目下的所有知识点
        List<Long> kpIds = knowledgePointMapper.selectList(
                new LambdaQueryWrapper<com.studyagent.entity.KnowledgePoint>()
                        .eq(com.studyagent.entity.KnowledgePoint::getSubjectId, subjectId)
        ).stream()
                .map(kp -> kp.getId())
                .toList();

        Map<Long, Double> masteryMap = new HashMap<>();

        // 初始化所有知识点掌握度为0
        for (Long kpId : kpIds) {
            masteryMap.put(kpId, 0.0);
        }

        // 查询学生已掌握的记录
        if (!kpIds.isEmpty()) {
            List<StudentKnowledgeMastery> masteries = masteryMapper.selectList(
                    new LambdaQueryWrapper<StudentKnowledgeMastery>()
                            .eq(StudentKnowledgeMastery::getStudentId, studentId)
                            .in(StudentKnowledgeMastery::getKnowledgePointId, kpIds)
            );

            for (StudentKnowledgeMastery m : masteries) {
                masteryMap.put(m.getKnowledgePointId(), m.getMasteryLevel());
            }
        }

        return masteryMap;
    }

    /**
     * 计算星级
     */
    private int calculateStarLevel(double rate) {
        if (rate >= 0.9) return 5;
        if (rate >= 0.8) return 4;
        if (rate >= 0.6) return 3;
        if (rate >= 0.4) return 2;
        if (rate >= 0.2) return 1;
        return 0;
    }
}

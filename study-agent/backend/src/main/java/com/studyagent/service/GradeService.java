package com.studyagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyagent.entity.Grade;
import com.studyagent.mapper.GradeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeMapper gradeMapper;

    public Grade create(String name, String stage, Integer sortOrder) {
        Grade grade = new Grade();
        grade.setName(name);
        grade.setStage(stage);
        grade.setSortOrder(sortOrder);
        gradeMapper.insert(grade);
        return grade;
    }

    public List<Grade> findAll() {
        return gradeMapper.selectList(
                new LambdaQueryWrapper<Grade>()
                        .orderByAsc(Grade::getSortOrder)
        );
    }

    public Grade findById(Long id) {
        return gradeMapper.selectById(id);
    }

    public void initDefaultGrades() {
        if (gradeMapper.selectCount(null) > 0) {
            return;
        }

        String[][] grades = {
                {"小学一年级", "小学", "1"},
                {"小学二年级", "小学", "2"},
                {"小学三年级", "小学", "3"},
                {"小学四年级", "小学", "4"},
                {"小学五年级", "小学", "5"},
                {"小学六年级", "小学", "6"},
                {"初中一年级", "初中", "7"},
                {"初中二年级", "初中", "8"},
                {"初中三年级", "初中", "9"},
                {"高中一年级", "高中", "10"},
                {"高中二年级", "高中", "11"},
                {"高中三年级", "高中", "12"}
        };

        for (String[] g : grades) {
            create(g[0], g[1], Integer.parseInt(g[2]));
        }
    }
}

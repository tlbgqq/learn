package com.studyagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyagent.entity.Subject;
import com.studyagent.mapper.SubjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectMapper subjectMapper;

    public Subject create(String name, Long gradeId, String textbookVersion, Integer sortOrder) {
        Subject subject = new Subject();
        subject.setName(name);
        subject.setGradeId(gradeId);
        subject.setTextbookVersion(textbookVersion);
        subject.setSortOrder(sortOrder);
        subjectMapper.insert(subject);
        return subject;
    }

    public Subject findById(Long id) {
        return subjectMapper.selectById(id);
    }

    public List<Subject> findByGradeId(Long gradeId) {
        return subjectMapper.selectList(
                new LambdaQueryWrapper<Subject>()
                        .eq(Subject::getGradeId, gradeId)
                        .orderByAsc(Subject::getSortOrder)
        );
    }

    public void initDefaultSubjects() {
        if (subjectMapper.selectCount(null) > 0) {
            return;
        }

        String[][] subjects = {
                {"语文", "1", "人教版", "1"},
                {"数学", "1", "人教版", "2"},
                {"英语", "1", "人教版", "3"}
        };

        for (String[] s : subjects) {
            create(s[0], Long.parseLong(s[1]), s[2], Integer.parseInt(s[3]));
        }
    }
}

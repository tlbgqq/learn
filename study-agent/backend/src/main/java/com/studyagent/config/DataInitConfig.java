package com.studyagent.config;

import com.studyagent.service.ApiKeyService;
import com.studyagent.service.GradeService;
import com.studyagent.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitConfig implements CommandLineRunner {

    private final GradeService gradeService;
    private final SubjectService subjectService;
    private final ApiKeyService apiKeyService;

    @Override
    public void run(String... args) {
        gradeService.initDefaultGrades();
        subjectService.initDefaultSubjects();
        apiKeyService.loadApiKeysToCache();
    }
}

package com.studyagent.service;

import com.studyagent.ai.QuestionAnalysisService;
import com.studyagent.entity.StudentAnswer;
import com.studyagent.mapper.StudentAnswerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncAnalysisService {

    private final QuestionAnalysisService analysisService;
    private final StudentAnswerMapper studentAnswerMapper;

    @Async("aiAnalysisExecutor")
    public void analyzeWrongAnswerAsync(Long studentAnswerId,
                                          String questionContent,
                                          String studentAnswer,
                                          String correctAnswer) {
        try {
            log.info("开始异步分析错题，studentAnswerId: {}", studentAnswerId);
            
            QuestionAnalysisService.AnalysisResult analysis = analysisService.analyzeWrongAnswer(
                    questionContent,
                    studentAnswer,
                    correctAnswer
            );
            
            StudentAnswer answer = studentAnswerMapper.selectById(studentAnswerId);
            if (answer != null) {
                answer.setErrorType(analysis.errorType());
                answer.setAiAnalysis(analysis.practiceSuggestion());
                answer.setMasteryLevel(analysis.masteryLevel());
                studentAnswerMapper.updateById(answer);
                log.info("异步分析完成，studentAnswerId: {}", studentAnswerId);
            } else {
                log.warn("未找到答题记录，studentAnswerId: {}", studentAnswerId);
            }
        } catch (Exception e) {
            log.error("异步分析错题失败，studentAnswerId: {}", studentAnswerId, e);
        }
    }
}

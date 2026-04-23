package com.studyagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyagent.entity.Achievement;
import com.studyagent.entity.DailyTask;
import com.studyagent.entity.StudentAchievement;
import com.studyagent.entity.StudentDailyTask;
import com.studyagent.mapper.AchievementMapper;
import com.studyagent.mapper.DailyTaskMapper;
import com.studyagent.mapper.StudentAchievementMapper;
import com.studyagent.mapper.StudentDailyTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardService {

    private static final int BASE_GOLD = 10;
    private static final int BASE_EXP = 5;
    private final AchievementMapper achievementMapper;
    private final StudentAchievementMapper studentAchievementMapper;
    private final DailyTaskMapper dailyTaskMapper;
    private final StudentDailyTaskMapper studentDailyTaskMapper;
    private final StudentService studentService;

    @Transactional
    public RewardResult calculateReward(Long studentId, boolean correct, int comboCount, int difficulty) {
        if (!correct) {
            return new RewardResult(0, 0, 0);
        }

        double comboMultiplier = 1 + (comboCount * 0.2);
        double difficultyBonus = 1 + (difficulty * 0.1);

        int gold = (int) (BASE_GOLD * comboMultiplier * difficultyBonus);
        int exp = (int) (BASE_EXP * comboMultiplier * difficultyBonus);
        int diamond = comboCount >= 5 ? 1 : 0;

        studentService.addExpAndGold(studentId, exp, gold);
        if (diamond > 0) {
            studentService.addDiamond(studentId, diamond);
        }

        checkAchievements(studentId);

        return new RewardResult(gold, exp, diamond);
    }

    public void checkAchievements(Long studentId) {
        List<Achievement> achievements = achievementMapper.selectList(null);
        for (Achievement ach : achievements) {
            boolean alreadyEarned = studentAchievementMapper.exists(
                    new LambdaQueryWrapper<StudentAchievement>()
                            .eq(StudentAchievement::getStudentId, studentId)
                            .eq(StudentAchievement::getAchievementId, ach.getId())
            );
            if (alreadyEarned) continue;

            boolean earned = switch (ach.getConditionType()) {
                case "CORRECT_COUNT" -> checkCorrectCount(studentId, ach.getConditionValue());
                case "COMBO" -> checkCombo(studentId, ach.getConditionValue());
                case "KNOWLEDGE_STAR" -> checkKnowledgeStar(studentId, ach.getConditionValue());
                case "CONTINUOUS_DAYS" -> checkContinuousDays(studentId, ach.getConditionValue());
                default -> false;
            };

            if (earned) {
                grantAchievement(studentId, ach);
            }
        }
    }

    private boolean checkCorrectCount(Long studentId, int target) {
        return false;
    }

    private boolean checkCombo(Long studentId, int target) {
        return false;
    }

    private boolean checkKnowledgeStar(Long studentId, int star) {
        return false;
    }

    private boolean checkContinuousDays(Long studentId, int days) {
        return false;
    }

    @Transactional
    public void grantAchievement(Long studentId, Achievement achievement) {
        StudentAchievement sa = new StudentAchievement();
        sa.setStudentId(studentId);
        sa.setAchievementId(achievement.getId());
        studentAchievementMapper.insert(sa);

        studentService.addExpAndGold(studentId, achievement.getRewardExp(), achievement.getRewardGold());
        if (achievement.getRewardDiamond() > 0) {
            studentService.addDiamond(studentId, achievement.getRewardDiamond());
        }
    }

    public List<Achievement> getAllAchievements() {
        return achievementMapper.selectList(null);
    }

    public List<StudentAchievement> getStudentAchievements(Long studentId) {
        return studentAchievementMapper.selectList(
                new LambdaQueryWrapper<StudentAchievement>()
                        .eq(StudentAchievement::getStudentId, studentId)
        );
    }

    public List<DailyTask> getAllDailyTasks() {
        return dailyTaskMapper.selectList(null);
    }

    public List<StudentDailyTask> getStudentDailyTasks(Long studentId) {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return studentDailyTaskMapper.selectList(
                new LambdaQueryWrapper<StudentDailyTask>()
                        .eq(StudentDailyTask::getStudentId, studentId)
                        .ge(StudentDailyTask::getDate, today)
        );
    }

    @Transactional
    public void updateDailyTaskProgress(Long studentId, String taskType, int progress) {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);

        List<DailyTask> tasks = dailyTaskMapper.selectList(
                new LambdaQueryWrapper<DailyTask>()
                        .eq(DailyTask::getTaskType, taskType)
        );

        for (DailyTask task : tasks) {
            StudentDailyTask studentTask = studentDailyTaskMapper.selectOne(
                    new LambdaQueryWrapper<StudentDailyTask>()
                            .eq(StudentDailyTask::getStudentId, studentId)
                            .eq(StudentDailyTask::getTaskId, task.getId())
                            .ge(StudentDailyTask::getDate, today)
            );

            if (studentTask == null) {
                studentTask = new StudentDailyTask();
                studentTask.setStudentId(studentId);
                studentTask.setTaskId(task.getId());
                studentTask.setProgress(0);
                studentTask.setCompleted(false);
                studentTask.setDate(today);
            }

            studentTask.setProgress(progress);
            if (progress >= task.getTargetCount() && !studentTask.getCompleted()) {
                studentTask.setCompleted(true);
                studentTask.setCompletedTime(LocalDateTime.now());
                studentService.addExpAndGold(studentId, task.getRewardExp(), task.getRewardGold());
            }

            if (studentTask.getId() == null) {
                studentDailyTaskMapper.insert(studentTask);
            } else {
                studentDailyTaskMapper.updateById(studentTask);
            }
        }
    }

    public record RewardResult(int gold, int exp, int diamond) {}
}

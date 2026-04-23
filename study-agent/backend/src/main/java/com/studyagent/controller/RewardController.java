package com.studyagent.controller;

import com.studyagent.entity.Achievement;
import com.studyagent.entity.DailyTask;
import com.studyagent.entity.StudentAchievement;
import com.studyagent.entity.StudentDailyTask;
import com.studyagent.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reward")
@RequiredArgsConstructor
@CrossOrigin
public class RewardController {

    private final RewardService rewardService;

    @GetMapping("/achievements")
    public ResponseEntity<List<Achievement>> getAllAchievements() {
        return ResponseEntity.ok(rewardService.getAllAchievements());
    }

    @GetMapping("/achievements/{studentId}")
    public ResponseEntity<List<StudentAchievement>> getStudentAchievements(@PathVariable Long studentId) {
        return ResponseEntity.ok(rewardService.getStudentAchievements(studentId));
    }

    @GetMapping("/daily-tasks")
    public ResponseEntity<List<DailyTask>> getAllDailyTasks() {
        return ResponseEntity.ok(rewardService.getAllDailyTasks());
    }

    @GetMapping("/daily-tasks/{studentId}")
    public ResponseEntity<List<StudentDailyTask>> getStudentDailyTasks(@PathVariable Long studentId) {
        return ResponseEntity.ok(rewardService.getStudentDailyTasks(studentId));
    }

    @PostMapping("/daily-tasks/progress")
    public ResponseEntity<Void> updateDailyTaskProgress(
            @RequestParam Long studentId,
            @RequestParam String taskType,
            @RequestParam int progress) {
        rewardService.updateDailyTaskProgress(studentId, taskType, progress);
        return ResponseEntity.ok().build();
    }
}

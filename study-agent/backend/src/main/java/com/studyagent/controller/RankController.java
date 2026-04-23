package com.studyagent.controller;

import com.studyagent.dto.RankResponse;
import com.studyagent.entity.Student;
import com.studyagent.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/rank")
@RequiredArgsConstructor
@CrossOrigin
public class RankController {

    private final StudentMapper studentMapper;

    @GetMapping("/week")
    public ResponseEntity<List<RankResponse>> getWeekRank() {
        List<Student> students = studentMapper.selectList(null);
        students.sort((a, b) -> b.getGold().compareTo(a.getGold()));

        List<RankResponse> ranks = new ArrayList<>();
        for (int i = 0; i < Math.min(students.size(), 20); i++) {
            Student s = students.get(i);
            ranks.add(new RankResponse(
                    s.getId(),
                    s.getNickname(),
                    s.getLevel(),
                    s.getGold(),
                    i + 1
            ));
        }
        return ResponseEntity.ok(ranks);
    }
}

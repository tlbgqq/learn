package com.studyagent.controller;

import com.studyagent.config.JwtInterceptor;
import com.studyagent.dto.LoginRequest;
import com.studyagent.dto.RegisterRequest;
import com.studyagent.entity.Student;
import com.studyagent.service.StudentService;
import com.studyagent.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@CrossOrigin
public class StudentController {

    private final StudentService studentService;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            Student student = studentService.register(
                    request.getUsername(),
                    request.getPassword(),
                    request.getNickname(),
                    request.getGradeId()
            );
            String token = jwtUtils.generateToken(student.getId(), student.getUsername());
            Map<String, Object> result = new HashMap<>();
            result.put("student", student);
            result.put("token", token);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Student student = studentService.login(request.getUsername(), request.getPassword());
        if (student != null) {
            String token = jwtUtils.generateToken(student.getId(), student.getUsername());
            Map<String, Object> result = new HashMap<>();
            result.put("student", student);
            result.put("token", token);
            return ResponseEntity.ok(result);
        }
        Map<String, String> error = new HashMap<>();
        error.put("message", "用户名或密码错误");
        return ResponseEntity.badRequest().body(error);
    }

    @GetMapping("/me")
    public ResponseEntity<Student> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(JwtInterceptor.USER_ID_HEADER);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Student student = studentService.findById(userId);
        if (student != null) {
            return ResponseEntity.ok(student);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getProfile(@PathVariable Long id) {
        Student student = studentService.findById(id);
        if (student != null) {
            return ResponseEntity.ok(student);
        }
        return ResponseEntity.notFound().build();
    }
}

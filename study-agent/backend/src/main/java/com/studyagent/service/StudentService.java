package com.studyagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyagent.entity.Student;
import com.studyagent.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private static final String SALT = "StudyAgent_Secret_Key_2024";
    private final StudentMapper studentMapper;

    @Transactional
    public Student register(String username, String password, String nickname, Integer gradeId) {
        if (studentMapper.exists(new LambdaQueryWrapper<Student>().eq(Student::getUsername, username))) {
            throw new RuntimeException("用户名已存在");
        }
        Student student = new Student();
        student.setUsername(username);
        student.setPassword(encryptPassword(password));
        student.setNickname(nickname);
        student.setGradeId(gradeId);
        student.setExp(0);
        student.setGold(0);
        student.setDiamond(0);
        student.setLevel(1);
        student.setContinuousStudyDays(0);
        studentMapper.insert(student);
        return student;
    }

    public Student login(String username, String password) {
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getUsername, username)
        );
        if (student != null && encryptPassword(password).equals(student.getPassword())) {
            return student;
        }
        return null;
    }

    private String encryptPassword(String password) {
        String salted = password + SALT;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(salted.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    public Student findById(Long id) {
        return studentMapper.selectById(id);
    }

    public List<Student> findAll() {
        return studentMapper.selectList(null);
    }

    @Transactional
    public void addExpAndGold(Long studentId, Integer exp, Integer gold) {
        Student student = studentMapper.selectById(studentId);
        if (student != null) {
            student.setExp(student.getExp() + exp);
            student.setGold(student.getGold() + gold);
            checkLevelUp(student);
            studentMapper.updateById(student);
        }
    }

    @Transactional
    public void addDiamond(Long studentId, Integer diamond) {
        Student student = studentMapper.selectById(studentId);
        if (student != null) {
            student.setDiamond(student.getDiamond() + diamond);
            studentMapper.updateById(student);
        }
    }

    private void checkLevelUp(Student student) {
        int expForNextLevel = student.getLevel() * 100;
        while (student.getExp() >= expForNextLevel) {
            student.setExp(student.getExp() - expForNextLevel);
            student.setLevel(student.getLevel() + 1);
        }
    }

    @Transactional
    public void updateContinuousStudyDays(Long studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student != null) {
            student.setContinuousStudyDays(student.getContinuousStudyDays() + 1);
            studentMapper.updateById(student);
        }
    }
}

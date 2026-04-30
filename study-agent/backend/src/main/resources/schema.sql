-- 学习辅助智能体数据库初始化脚本

CREATE DATABASE IF NOT EXISTS study_agent DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE study_agent;

-- 年级表
CREATE TABLE IF NOT EXISTS t_grade (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(20) NOT NULL DEFAULT '' COMMENT '年级名称',
    stage VARCHAR(10) NOT NULL DEFAULT '' COMMENT '学段：小学/初中/高中',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    del TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) COMMENT='年级表';

-- 学科表
CREATE TABLE IF NOT EXISTS t_subject (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(50) NOT NULL DEFAULT '' COMMENT '学科名称',
    grade_id BIGINT NOT NULL DEFAULT 0 COMMENT '所属年级ID',
    textbook_version VARCHAR(50) NOT NULL DEFAULT '' COMMENT '教材版本',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    del TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) COMMENT='学科表';

-- 知识点表
CREATE TABLE IF NOT EXISTS t_knowledge_point (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL DEFAULT '' COMMENT '知识点名称',
    code VARCHAR(50) NOT NULL DEFAULT '' COMMENT '知识点编码',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父知识点ID，0表示顶级',
    subject_id BIGINT NOT NULL DEFAULT 0 COMMENT '所属学科ID',
    grade_id BIGINT NOT NULL DEFAULT 0 COMMENT '所属年级ID',
    difficulty INT NOT NULL DEFAULT 1 COMMENT '难度等级：1-5星',
    description VARCHAR(500) NOT NULL DEFAULT '' COMMENT '知识点描述',
    common_errors TEXT NOT NULL COMMENT '常见错误类型',
    related_concepts TEXT NOT NULL COMMENT '关联知识点',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    del TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) COMMENT='知识点表';

-- 学生表
CREATE TABLE IF NOT EXISTS t_student (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL DEFAULT '' COMMENT '用户名',
    password VARCHAR(100) NOT NULL DEFAULT '' COMMENT '密码',
    nickname VARCHAR(100) NOT NULL DEFAULT '' COMMENT '昵称',
    avatar VARCHAR(500) NOT NULL DEFAULT '' COMMENT '头像URL',
    grade_id INT NOT NULL DEFAULT 0 COMMENT '年级ID',
    class_id BIGINT NOT NULL DEFAULT 0 COMMENT '班级ID',
    exp INT NOT NULL DEFAULT 0 COMMENT '经验值',
    gold INT NOT NULL DEFAULT 0 COMMENT '金币数量',
    diamond INT NOT NULL DEFAULT 0 COMMENT '钻石数量',
    level INT NOT NULL DEFAULT 1 COMMENT '等级',
    continuous_study_days INT NOT NULL DEFAULT 0 COMMENT '连续学习天数',
    del TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) COMMENT='学生表';

-- 题目表
CREATE TABLE IF NOT EXISTS t_question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    type VARCHAR(10) NOT NULL DEFAULT '' COMMENT '题目类型：选择/填空/解答',
    content TEXT NOT NULL DEFAULT '' COMMENT '题目内容（选择题不包含选项）',
    options TEXT NOT NULL DEFAULT '' COMMENT '选择题选项，JSON格式如：["A. xxx","B. xxx","C. xxx","D. xxx"]',
    answer TEXT NOT NULL DEFAULT '' COMMENT '正确答案',
    analysis TEXT NOT NULL DEFAULT '' COMMENT '题目解析',
    subject_id BIGINT NOT NULL DEFAULT 0 COMMENT '所属学科ID',
    knowledge_point_ids TEXT NOT NULL DEFAULT '' COMMENT '关联知识点ID，多个用逗号分隔',
    difficulty INT NOT NULL DEFAULT 1 COMMENT '难度等级：1-5星',
    frequency INT NOT NULL DEFAULT 0 COMMENT '出题频率',
    del TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) COMMENT='题目表';

-- 学生答题记录表
CREATE TABLE IF NOT EXISTS t_student_answer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    student_id BIGINT NOT NULL DEFAULT 0 COMMENT '学生ID',
    question_id BIGINT NOT NULL DEFAULT 0 COMMENT '题目ID',
    answer TEXT NOT NULL COMMENT '学生答案',
    is_correct TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否正确：0-错误，1-正确',
    error_type VARCHAR(50) NOT NULL DEFAULT '' COMMENT '错误类型：知识点缺失/理解偏差/审题错误/计算失误',
    ai_analysis TEXT NOT NULL COMMENT 'AI分析结果',
    knowledge_point_id BIGINT NOT NULL DEFAULT 0 COMMENT '关联知识点ID',
    mastery_level DOUBLE NOT NULL DEFAULT 0.0 COMMENT '掌握度：0.0-1.0',
    corrected TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已改正：0-未改正，1-已改正',
    del TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) COMMENT='学生答题记录表';

-- 为已存在的表添加 corrected 字段（迁移用）
ALTER TABLE t_student_answer ADD COLUMN IF NOT EXISTS corrected TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已改正：0-未改正，1-已改正';

-- 学生知识点掌握度表
CREATE TABLE IF NOT EXISTS t_student_knowledge_mastery (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    student_id BIGINT NOT NULL DEFAULT 0 COMMENT '学生ID',
    knowledge_point_id BIGINT NOT NULL DEFAULT 0 COMMENT '知识点ID',
    mastery_level DOUBLE NOT NULL DEFAULT 0.0 COMMENT '掌握度：0.0-1.0',
    star_level INT NOT NULL DEFAULT 0 COMMENT '星级：0-5星',
    practice_count INT NOT NULL DEFAULT 0 COMMENT '练习次数',
    correct_count INT NOT NULL DEFAULT 0 COMMENT '正确次数',
    del TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_student_kp (student_id, knowledge_point_id)
) COMMENT='学生知识点掌握度表';

-- 成就表
CREATE TABLE IF NOT EXISTS t_achievement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(50) NOT NULL DEFAULT '' COMMENT '成就名称',
    icon VARCHAR(10) NOT NULL DEFAULT '' COMMENT '成就图标',
    description VARCHAR(200) NOT NULL DEFAULT '' COMMENT '成就描述',
    condition_type VARCHAR(30) NOT NULL DEFAULT '' COMMENT '达成条件类型：CORRECT_COUNT/COMBO/KNOWLEDGE_STAR/CONTINUOUS_DAYS',
    condition_value INT NOT NULL DEFAULT 0 COMMENT '达成条件值',
    reward_gold INT NOT NULL DEFAULT 0 COMMENT '奖励金币',
    reward_exp INT NOT NULL DEFAULT 0 COMMENT '奖励经验',
    reward_diamond INT NOT NULL DEFAULT 0 COMMENT '奖励钻石',
    del TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) COMMENT='成就表';

-- 学生已获得成就表
CREATE TABLE IF NOT EXISTS t_student_achievement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    student_id BIGINT NOT NULL DEFAULT 0 COMMENT '学生ID',
    achievement_id BIGINT NOT NULL DEFAULT 0 COMMENT '成就ID',
    del TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_student_ach (student_id, achievement_id)
) COMMENT='学生已获得成就表';

-- 每日任务表
CREATE TABLE IF NOT EXISTS t_daily_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(50) NOT NULL DEFAULT '' COMMENT '任务名称',
    icon VARCHAR(10) NOT NULL DEFAULT '' COMMENT '任务图标',
    description VARCHAR(200) NOT NULL DEFAULT '' COMMENT '任务描述',
    target_count INT NOT NULL DEFAULT 0 COMMENT '目标数量',
    reward_gold INT NOT NULL DEFAULT 0 COMMENT '奖励金币',
    reward_exp INT NOT NULL DEFAULT 0 COMMENT '奖励经验',
    task_type VARCHAR(30) NOT NULL DEFAULT '' COMMENT '任务类型：ANSWER_COUNT/CORRECT_STREAK/STUDY_TIME',
    del TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) COMMENT='每日任务表';

-- 学生每日任务进度表
CREATE TABLE IF NOT EXISTS t_student_daily_task (
                                                    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                                                    student_id BIGINT NOT NULL DEFAULT 0 COMMENT '学生ID',
                                                    task_id BIGINT NOT NULL DEFAULT 0 COMMENT '任务ID',
                                                    progress INT NOT NULL DEFAULT 0 COMMENT '当前进度',
                                                    completed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否完成：0-未完成，1-已完成',
    completed_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '完成时间',
    date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '任务日期',
    del TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_student_task_date (student_id, task_id, date)
    ) COMMENT='学生每日任务进度表';

-- 索引
CREATE INDEX idx_student_grade ON t_student(grade_id);
CREATE INDEX idx_student_del ON t_student(del);
CREATE INDEX idx_kp_subject ON t_knowledge_point(subject_id);
CREATE INDEX idx_kp_grade ON t_knowledge_point(grade_id);
CREATE INDEX idx_kp_del ON t_knowledge_point(del);
CREATE INDEX idx_question_subject ON t_question(subject_id);
CREATE INDEX idx_question_del ON t_question(del);
CREATE INDEX idx_answer_student ON t_student_answer(student_id);
CREATE INDEX idx_answer_question ON t_student_answer(question_id);
CREATE INDEX idx_answer_del ON t_student_answer(del);
CREATE INDEX idx_grade_del ON t_grade(del);
CREATE INDEX idx_subject_grade ON t_subject(grade_id);
CREATE INDEX idx_subject_del ON t_subject(del);
CREATE INDEX idx_mastery_student ON t_student_knowledge_mastery(student_id);
CREATE INDEX idx_mastery_del ON t_student_knowledge_mastery(del);
CREATE INDEX idx_ach_del ON t_achievement(del);
CREATE INDEX idx_student_ach_del ON t_student_achievement(del);
CREATE INDEX idx_task_del ON t_daily_task(del);
CREATE INDEX idx_student_task_del ON t_student_daily_task(del);

-- =====================================================
-- 冲刺模式相关表
-- =====================================================

-- 冲刺记录表
CREATE TABLE IF NOT EXISTS t_sprint_record (
                                               id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                                               session_id VARCHAR(50) NOT NULL DEFAULT '' COMMENT '会话ID',
    student_id BIGINT NOT NULL DEFAULT 0 COMMENT '学生ID',
    subject_id BIGINT NOT NULL DEFAULT 0 COMMENT '学科ID',
    total_score INT NOT NULL DEFAULT 0 COMMENT '总得分',
    total_questions INT NOT NULL DEFAULT 0 COMMENT '总题数',
    correct_count INT NOT NULL DEFAULT 0 COMMENT '正确数',
    wrong_count INT NOT NULL DEFAULT 0 COMMENT '错误数',
    accuracy DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '正确率',
    max_combo INT NOT NULL DEFAULT 0 COMMENT '最高连击',
    duration INT NOT NULL DEFAULT 0 COMMENT '实际用时（秒）',
    end_reason VARCHAR(20) NOT NULL DEFAULT '' COMMENT '结束原因：TIMEOUT/USER_END/SURRENDER',
    is_new_record TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否创新纪录',
    del TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_session (session_id)
    ) COMMENT='冲刺模式记录表';

-- 冲刺会话表
CREATE TABLE IF NOT EXISTS t_sprint_session (
                                                id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                                                session_id VARCHAR(50) NOT NULL DEFAULT '' COMMENT '会话ID',
    student_id BIGINT NOT NULL DEFAULT 0 COMMENT '学生ID',
    subject_id BIGINT NOT NULL DEFAULT 0 COMMENT '学科ID',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/FINISHED/EXPIRED',
    used_question_ids TEXT NOT NULL COMMENT '已使用题目ID，JSON格式',
    del TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_session (session_id)
    ) COMMENT='冲刺模式会话表';

CREATE INDEX idx_t_sprint_record_student ON t_sprint_record (student_id);

CREATE INDEX idx_t_sprint_record_student_date ON t_sprint_record (student_id, create_time);

CREATE INDEX idx_t_sprint_session_student ON t_sprint_session (student_id);

CREATE INDEX idx_t_sprint_session_status ON t_sprint_session (status);

-- 初始化年级数据
INSERT INTO t_grade (name, stage, sort_order) VALUES
('小学一年级', '小学', 1),
('小学二年级', '小学', 2),
('小学三年级', '小学', 3),
('小学四年级', '小学', 4),
('小学五年级', '小学', 5),
('小学六年级', '小学', 6),
('初中一年级', '初中', 7),
('初中二年级', '初中', 8),
('初中三年级', '初中', 9),
('高中一年级', '高中', 10),
('高中二年级', '高中', 11),
('高中三年级', '高中', 12);

-- 初始化学科数据（以小学一年级为例）
INSERT INTO t_subject (name, grade_id, textbook_version, sort_order) VALUES
('语文', 1, '人教版', 1),
('数学', 1, '人教版', 2),
('英语', 1, '人教版', 3);

-- 初始化学科数据（小学三年级英语）
INSERT INTO t_subject (name, grade_id, textbook_version, sort_order) VALUES
('英语', 6, '人教版', 3);

-- 初始化知识点数据（数学小学一年级为例）
INSERT INTO t_knowledge_point (name, code, parent_id, subject_id, grade_id, difficulty, description, common_errors, related_concepts, sort_order) VALUES
('数与代数', 'MATH_1_1', 0, 2, 1, 1, '数的认识与运算', '', '', 1),
('1-5的认识', 'MATH_1_1_1', 1, 2, 1, 1, '认识1-5，学会数数', '', '', 1),
('比大小', 'MATH_1_1_2', 1, 2, 1, 1, '比较两个数的大小', '', '', 2),
('加减法', 'MATH_1_1_3', 1, 2, 1, 2, '10以内加减法运算', '', '', 3),
('图形认识', 'MATH_1_2', 0, 2, 1, 1, '认识基本图形', '', '', 2),
('正方形', 'MATH_1_2_1', 5, 2, 1, 1, '认识正方形', '', '', 1),
('圆形', 'MATH_1_2_2', 5, 2, 1, 1, '认识圆形', '', '', 2);

-- 初始化知识点数据（英语小学三年级为例）
INSERT INTO t_knowledge_point (name, code, parent_id, subject_id, grade_id, difficulty, description, common_errors, related_concepts, sort_order) VALUES
-- 字母学习模块
('英语字母', 'ENGLISH_ABC', 0, 3, 6, 1, '26个英文字母学习', '', '', 1),
('字母 Aa-Hh', 'ENGLISH_A_H', 1, 3, 6, 1, '学习字母Aa到Hh的发音和书写', '', '', 1),
('字母 Ii-Rr', 'ENGLISH_I_R', 1, 3, 6, 1, '学习字母Ii到Rr的发音和书写', '', '', 2),
('字母 Ss-Zz', 'ENGLISH_S_Z', 1, 3, 6, 1, '学习字母Ss到Zz的发音和书写', '', '', 3),
-- 第一单元：Make Friends
('第一单元：Make Friends', 'ENGLISH_UNIT1', 0, 3, 6, 1, '学习如何用英语交朋友和打招呼', '', '', 2),
('问候语 Hi/Hello', 'ENGLISH_UNIT1_1', 5, 3, 6, 1, '学习Hi和Hello的区别和使用场景', '', '', 1),
('介绍自己 I am...', 'ENGLISH_UNIT1_2', 5, 3, 6, 1, '学习用I am...介绍自己的姓名和年龄', '', '', 2),
('询问姓名 What is your name?', 'ENGLISH_UNIT1_3', 5, 3, 6, 1, '学习询问对方姓名的表达方式', '', '', 3),
('询问年龄 How old are you?', 'ENGLISH_UNIT1_4', 5, 3, 6, 1, '学习询问对方年龄的表达方式', '', '', 4),
-- 第二单元：Colors
('第二单元：Colors', 'ENGLISH_UNIT2', 0, 3, 6, 1, '学习各种颜色的英语表达', '', '', 3),
('基础颜色词', 'ENGLISH_UNIT2_1', 9, 3, 6, 1, '学习red, blue, green, yellow等基础颜色词', '', '', 1),
('颜色词进阶', 'ENGLISH_UNIT2_2', 9, 3, 6, 2, '学习orange, purple, pink, brown等进阶颜色词', '', '', 2),
('颜色描述 It is...', 'ENGLISH_UNIT2_3', 9, 3, 6, 1, '学习用It is...描述物品颜色', '', '', 3),
-- 第三单元：Numbers
('第三单元：Numbers', 'ENGLISH_UNIT3', 0, 3, 6, 1, '学习1-20的数字表达', '', '', 4),
('数字1-10', 'ENGLISH_UNIT3_1', 13, 3, 6, 1, '学习one到ten的发音和拼写', '', '', 1),
('数字11-20', 'ENGLISH_UNIT3_2', 13, 3, 6, 2, '学习eleven到twenty的发音和拼写', '', '', 2),
('数量描述 I have...', 'ENGLISH_UNIT3_3', 13, 3, 6, 1, '学习用I have...表达拥有物品的数量', '', '', 3),
-- 第四单元：Family
('第四单元：Family', 'ENGLISH_UNIT4', 0, 3, 6, 1, '学习家庭成员的英语称呼', '', '', 5),
('家庭成员称谓', 'ENGLISH_UNIT4_1', 17, 3, 6, 1, '学习father, mother, brother, sister等称谓', '', '', 1),
('介绍家人 This is my...', 'ENGLISH_UNIT4_2', 17, 3, 6, 1, '学习用This is my...介绍家庭成员', '', '', 2),
('家人年龄 My father is...', 'ENGLISH_UNIT4_3', 17, 3, 6, 2, '学习描述家庭成员的年龄', '', '', 3),
-- 第五单元：Animals
('第五单元：Animals', 'ENGLISH_UNIT5', 0, 3, 6, 1, '学习常见动物的英语名称', '', '', 6),
('常见动物名称', 'ENGLISH_UNIT5_1', 21, 3, 6, 1, '学习cat, dog, bird, fish等常见动物', '', '', 1),
('农场动物', 'ENGLISH_UNIT5_2', 21, 3, 6, 1, '学习farm animal的英语表达', '', '', 2),
('动物描述 It is a...', 'ENGLISH_UNIT5_3', 21, 3, 6, 1, '学习用It is a...描述动物', '', '', 3),
-- 第六单元：Food
('第六单元：Food', 'ENGLISH_UNIT6', 0, 3, 6, 1, '学习常见食物的英语表达', '', '', 7),
('水果类食物', 'ENGLISH_UNIT6_1', 25, 3, 6, 1, '学习apple, banana, orange等水果名称', '', '', 1),
('蔬菜类食物', 'ENGLISH_UNIT6_2', 25, 3, 6, 1, '学习vegetable的英语表达', '', '', 2),
('饮料类', 'ENGLISH_UNIT6_3', 25, 3, 6, 1, '学习water, milk, juice等饮料名称', '', '', 3),
('喜好表达 I like...', 'ENGLISH_UNIT6_4', 25, 3, 6, 1, '学习用I like...表达喜好', '', '', 4),
-- 语法基础
('语法基础', 'ENGLISH_GRAMMAR', 0, 3, 6, 2, '英语基础语法入门', '', '', 8),
('be动词用法', 'ENGLISH_GRAMMAR_BE', 31, 3, 6, 2, '学习am, is, are的用法和区别', '', '', 1),
('主格代词', 'ENGLISH_GRAMMAR_PRONOUN', 31, 3, 6, 2, '学习I, you, he, she, it的用法', '', '', 2),
('指示代词 this/that', 'ENGLISH_GRAMMAR_DEMO', 31, 3, 6, 2, '学习this和that的区别和使用', '', '', 3),
('物主代词 my/your', 'ENGLISH_GRAMMAR_POSS', 31, 3, 6, 2, '学习my和your的用法', '', '', 4),
-- 常用表达
('常用表达', 'ENGLISH_EXPRESSIONS', 0, 3, 6, 1, '日常交际常用英语表达', '', '', 9),
('道别 Goodbye/Bye', 'ENGLISH_EXP_GOODBYE', 36, 3, 6, 1, '学习道别的表达方式', '', '', 1),
('感谢表达 Thank you', 'ENGLISH_EXP_THANKS', 36, 3, 6, 1, '学习感谢的表达方式', '', '', 2),
('道歉表达 Sorry', 'ENGLISH_EXP_SORRY', 36, 3, 6, 1, '学习道歉的表达方式', '', '', 3),
('请求表达 Please', 'ENGLISH_EXP_PLEASE', 36, 3, 6, 1, '学习礼貌请求的表达方式', '', '', 4);

-- 初始化知识点数据（英语小学三年级下学期）
INSERT INTO t_knowledge_point (name, code, parent_id, subject_id, grade_id, difficulty, description, common_errors, related_concepts, sort_order) VALUES
-- 第一单元：Welcome back to school
('第一单元：Welcome back to school', 'ENGLISH_G2_UNIT1', 0, 3, 6, 1, '欢迎回到学校，学习国家和人称区分', '', '', 10),
('国家名称', 'ENGLISH_G2_UNIT1_1', 41, 3, 6, 1, '学习China, USA, UK, Canada, Australia等', '', '', 1),
('问候与欢迎 Welcome', 'ENGLISH_G2_UNIT1_2', 41, 3, 6, 1, '学习Welcome back, Nice to see you等', '', '', 2),
('人称区分 She/He', 'ENGLISH_G2_UNIT1_3', 41, 3, 6, 1, '学习she和he的区别和使用', '', '', 3),
('国籍表达 She is from...', 'ENGLISH_G2_UNIT1_4', 41, 3, 6, 2, '学习描述他人的国籍', '', '', 4),
-- 第二单元：My family
('第二单元：My family', 'ENGLISH_G2_UNIT2', 0, 3, 6, 1, '学习更多家庭成员称谓', '', '', 11),
('家庭成员称谓扩充', 'ENGLISH_G2_UNIT2_1', 46, 3, 6, 1, '学习grandfather, grandmother, uncle, aunt等', '', '', 1),
('介绍长辈 This is my...', 'ENGLISH_G2_UNIT2_2', 46, 3, 6, 1, '学习介绍祖父母等家庭成员', '', '', 2),
('Who问句 Who is she?', 'ENGLISH_G2_UNIT2_3', 46, 3, 6, 2, '学习用Who is...?询问人物关系', '', '', 3),
-- 第三单元：At the zoo
('第三单元：At the zoo', 'ENGLISH_G2_UNIT3', 0, 3, 6, 1, '学习动物园动物及相关描述', '', '', 12),
('动物园动物名称', 'ENGLISH_G2_UNIT3_1', 50, 3, 6, 1, '学习elephant, tiger, lion, monkey, rabbit等', '', '', 1),
('动物描述 It has...', 'ENGLISH_G2_UNIT3_2', 50, 3, 6, 1, '学习用It has...描述动物特征', '', '', 2),
('复数概念 animals', 'ENGLISH_G2_UNIT3_3', 50, 3, 6, 2, '了解可数名词复数形式', '', '', 3),
-- 第四单元：Where is my car?
('第四单元：Where is my car?', 'ENGLISH_G2_UNIT4', 0, 3, 6, 1, '学习方位表达', '', '', 13),
('方位词 in/on/under', 'ENGLISH_G2_UNIT4_1', 54, 3, 6, 1, '学习in, on, under的区别和使用', '', '', 1),
('方位词 near/behind', 'ENGLISH_G2_UNIT4_2', 54, 3, 6, 1, '学习near, behind, in front of等方位词', '', '', 2),
('Where问句 Where is...?', 'ENGLISH_G2_UNIT4_3', 54, 3, 6, 1, '学习用Where is...?询问位置', '', '', 3),
-- 第五单元：Do you like pears?
('第五单元：Do you like pears?', 'ENGLISH_G2_UNIT5', 0, 3, 6, 1, '学习询问喜好的表达', '', '', 14),
('更多水果名称', 'ENGLISH_G2_UNIT5_1', 58, 3, 6, 1, '学习pear, watermelon, grape, strawberry等', '', '', 1),
('Do you like...? 句型', 'ENGLISH_G2_UNIT5_2', 58, 3, 6, 1, '学习用Do you like...?询问喜好', '', '', 2),
('Yes/No 回答', 'ENGLISH_G2_UNIT5_3', 58, 3, 6, 1, '学习Yes, I do. / No, I don\'t. 回答', '', '', 3),
-- 第六单元：How many?
('第六单元：How many?', 'ENGLISH_G2_UNIT6', 0, 3, 6, 1, '学习询问数量', '', '', 15),
('数字13-20', 'ENGLISH_G2_UNIT6_1', 62, 3, 6, 1, '学习thirteen到twenty的发音和拼写', '', '', 1),
('How many...? 句型', 'ENGLISH_G2_UNIT6_2', 62, 3, 6, 1, '学习用How many...?询问可数名词数量', '', '', 2),
('名词单复数', 'ENGLISH_G2_UNIT6_3', 62, 3, 6, 2, '可数名词单复数变化规则', '', '', 3),
-- 下学期语法汇总
('下学期语法汇总', 'ENGLISH_G2_GRAMMAR', 0, 3, 6, 2, '三年级下学期语法重点', '', '', 16),
('has用法', 'ENGLISH_G2_GRAMMAR_HAS', 66, 3, 6, 2, '学习has与have的区别和使用', '', '', 1),
('人称代词主格', 'ENGLISH_G2_GRAMMAR_SUBJ', 66, 3, 6, 2, '学习I, you, he, she, we, they的用法', '', '', 2),
('可数名词复数', 'ENGLISH_G2_GRAMMAR_PLURAL', 66, 3, 6, 2, '可数名词变复数的规则', '', '', 3);

-- 初始化每日任务
INSERT INTO t_daily_task (name, icon, description, target_count, reward_gold, reward_exp, task_type) VALUES
('每日答题', '📝', '完成10道题目', 10, 50, 20, 'ANSWER_COUNT'),
('连续答对', '🎯', '连续答对5题', 5, 30, 15, 'CORRECT_STREAK'),
('学习时长', '⏰', '学习30分钟', 30, 40, 20, 'STUDY_TIME');

-- 初始化成就
INSERT INTO t_achievement (name, icon, description, condition_type, condition_value, reward_gold, reward_exp, reward_diamond) VALUES
('初出茅庐', '🌱', '完成首次答题', 'CORRECT_COUNT', 1, 10, 5, 0),
('小试牛刀', '📖', '累计答对50题', 'CORRECT_COUNT', 50, 100, 50, 5),
('答题达人', '🏆', '累计答对200题', 'CORRECT_COUNT', 200, 300, 150, 20),
('连胜新星', '⭐', '单次练习达成5连击', 'COMBO', 5, 50, 30, 3),
('连胜达人', '🔥', '单次练习达成10连击', 'COMBO', 10, 100, 60, 5),
('知识探索者', '🔍', '掌握10个知识点', 'KNOWLEDGE_STAR', 10, 80, 40, 5),
('连续学习3天', '📅', '连续学习3天', 'CONTINUOUS_DAYS', 3, 50, 30, 2),
('连续学习7天', '🌟', '连续学习7天', 'CONTINUOUS_DAYS', 7, 150, 80, 10);

-- 小学三年级英语题目
-- 字母学习题
INSERT INTO t_question (type, content, options, answer, analysis, subject_id, knowledge_point_ids, difficulty, frequency) VALUES
('选择', '下列哪个是字母"B"的大写形式？', '["A. b","B. B","C. d","D. p"]', 'B', '考查字母大小写对应关系', 3, '2', 1, 95),
('填空', '字母表第一个字母是__。', '[]', 'A', '考查字母顺序', 3, '2', 1, 90),
('选择', '小写字母"a"的正确书写占哪两格？', '["A. 中格","B. 下一格","C. 上一格和中格","D. 上一格"]', 'C', '考查字母书写格式', 3, '2', 1, 85),

-- 第一单元 Make Friends
('选择', '你想向别人介绍自己，可以说：', '["A. What is your name?","B. I am...","C. How are you?","D. Nice to meet you."]', 'B', 'I am = I\'m，用于介绍自己', 3, '7', 1, 98),
('填空', '你的名字是Tom，应该回答：__ is Tom.', '[]', 'My name', '考查询问姓名的回答', 3, '8', 1, 95),
('选择', '询问对方年龄应该说：', '["A. What is your name?","B. How are you?","C. How old are you?","D. Nice to meet you."]', 'C', 'How old are you? 是询问年龄的习惯表达', 3, '9', 1, 98),
('填空', '我七岁了。__ seven years old.', '[]', 'I am', '考查用I am表达年龄', 3, '7', 1, 92),

-- 第二单元 Colors
('选择', '天空是什么颜色的？What color is the sky?', '["A. red","B. blue","C. green","D. yellow"]', 'B', '天空是蓝色的，blue', 3, '11', 1, 98),
('填空', '红色是__. Red is red.', '[]', 'red', '考查颜色单词', 3, '11', 1, 95),
('选择', '"orange"在英语中表示什么颜色？', '["A. 红色","B. 蓝色","C. 橙色","D. 紫色"]', 'C', 'orange表示橙色', 3, '12', 1, 92),
('填空', '这是一个黄色的球。It is a __ ball.', '[]', 'yellow', '考查颜色描述句型', 3, '13', 1, 90),

-- 第三单元 Numbers
('选择', '"five"表示哪个数字？', '["A. 4","B. 5","C. 6","D. 7"]', 'B', 'five = 5', 3, '14', 1, 98),
('填空', '三加二等于__. Three and two is __.', '[]', 'five', '考查简单加法和数字', 3, '14', 1, 95),
('选择', '"twelve"是数字多少？', '["A. 10","B. 11","C. 12","D. 13"]', 'C', 'twelve = 12', 3, '15', 1, 90),
('填空', '我有三支铅笔。I have __ pencils.', '[]', 'three', '考查I have...句型和数字', 3, '16', 1, 92),

-- 第四单元 Family
('选择', '"father"的中文意思是：', '["A. 妈妈","B. 爸爸","C. 哥哥","D. 姐姐"]', 'B', 'father = 爸爸', 3, '18', 1, 98),
('选择', '介绍自己的妈妈应该说：', '["A. This is my father.","B. This is my mother.","C. This is my sister.","D. This is my brother."]', 'C', 'This is my... 是介绍家人的句型', 3, '19', 1, 95),
('填空', '这是我的姐姐。__ is my sister.', '[]', 'This', '考查介绍家人的句型', 3, '19', 1, 92),

-- 第五单元 Animals
('选择', '"cat"是什么动物？', '["A. 狗","B. 猫","C. 鸟","D. 鱼"]', 'B', 'cat = 猫', 3, '22', 1, 98),
('选择', '下列哪个是农场动物？', '["A. tiger","B. lion","C. cow","D. monkey"]', 'C', 'cow是农场动物', 3, '23', 1, 92),
('填空', '它是一只狗。It is a __.', '[]', 'dog', '考查动物名称', 3, '22', 1, 95),

-- 第六单元 Food
('选择', '"apple"是什么意思？', '["A. 香蕉","B. 苹果","C. 橙子","D. 葡萄"]', 'B', 'apple = 苹果', 3, '26', 1, 98),
('填空', '我喜欢香蕉。I __ bananas.', '[]', 'like', '考查I like...句型', 3, '30', 1, 92),
('选择', '你喜欢梨吗？__ you like pears?', '["A. Do","B. Does","C. Is","D. Are"]', 'A', 'Do you like...? 疑问句句型', 3, '30', 1, 90),

-- 语法基础
('选择', '"I __ a student." 空白处应该填：', '["A. is","B. am","C. are","D. be"]', 'B', '第一人称I后用am', 3, '32', 1, 95),
('填空', '他是医生。__ is a doctor.', '[]', 'He', '考查主格代词he的用法', 3, '33', 1, 92),
('选择', '这个是什么？英文是：', '["A. What is this?","B. This is","C. Is this?","D. That is"]', 'B', 'this is = 这是', 3, '34', 1, 90),
('填空', '那是我的书。__ is my book.', '[]', 'That', '考查that的用法', 3, '34', 1, 88),
('选择', '你的书包在哪里？Where is __ bag?', '["A. I","B. my","C. me","D. mine"]', 'B', 'your表示你的', 3, '35', 1, 85),

-- 三年级下学期题目
-- 第一单元 Welcome back to school
('选择', '"China"的中文意思是：', '["A. 美国","B. 中国","C. 英国","D. 加拿大"]', 'B', 'China = 中国', 3, '42', 1, 98),
('选择', '她是女孩，应用哪个代词？', '["A. he","B. it","C. she","D. they"]', 'C', 'she表示女性', 3, '44', 1, 95),
('填空', '她来自美国。She is __ the USA.', '[]', 'from', '考查国籍表达', 3, '45', 1, 90),

-- 第二单元 My family
('选择', '"grandfather"是指：', '["A. 奶奶","B. 爷爷/外公","C. 叔叔","D. 阿姨"]', 'B', 'grandfather = 祖父/外祖父', 3, '47', 1, 98),
('填空', '她是谁？__ is she?', '[]', 'Who', '考查Who问句', 3, '49', 1, 92),

-- 第三单元 At the zoo
('选择', '"elephant"是什么动物？', '["A. 老虎","B. 大象","C. 狮子","D. 猴子"]', 'B', 'elephant = 大象', 3, '51', 1, 98),
('填空', '它有一条长鼻子。It __ a long nose.', '[]', 'has', '考查It has...描述动物特征', 3, '52', 1, 90),
('选择', '一只猫的复数形式是：', '["A. cat","B. cats","C. catz","D. cates"]', 'C', 'cat的复数是cats', 3, '53', 1, 88),

-- 第四单元 Where is my car?
('选择', '"under"的中文意思是：', '["A. 在...里面","B. 在...下面","C. 在...上面","D. 在...旁边"]', 'B', 'under = 在...下面', 3, '55', 1, 95),
('填空', '书在桌子上。The book is __ the desk.', '[]', 'on', '考查on表示在...上', 3, '55', 1, 92),
('选择', '我的汽车在哪里？Where is __ car?', '["A. I","B. my","C. me","D. mine"]', 'B', 'my表示我的', 3, '57', 1, 90),

-- 第五单元 Do you like pears?
('选择', '"watermelon"是什么意思？', '["A. 苹果","B. 西瓜","C. 葡萄","D. 香蕉"]', 'B', 'watermelon = 西瓜', 3, '59', 1, 95),
('选择', 'Do you like apples? 肯定回答是：', '["A. Yes, I do.","B. No, I don\'t.","C. Yes, I am.","D. No, I am not."]', 'A', 'Yes, I do. 表示喜欢', 3, '61', 1, 92),
('填空', '我喜欢葡萄。I __ grapes.', '[]', 'like', '考查I like句型', 3, '60', 1, 90),

-- 第六单元 How many?
('选择', '"fifteen"是数字多少？', '["A. 13","B. 14","C. 15","D. 16"]', 'C', 'fifteen = 15', 3, '63', 1, 98),
('选择', 'How many __ can you see? 你能看到多少只鸟？', '["A. bird","B. birds","C. a bird","D. the bird"]', 'C', 'birds是复数形式', 3, '65', 1, 90),
('填空', '我有十四个苹果。I have __ apples.', '[]', 'fourteen', '考查14的表达', 3, '63', 1, 92),

-- 语法综合
('选择', 'He __ a blue bag.', '["A. have","B. has","C. is","D. are"]', 'B', 'he后用has', 3, '67', 1, 95),
('填空', '我们是一家人。__ are a family.', '[]', 'We', '考查we的用法', 3, '68', 1, 90),
('选择', '下列哪个是错误的复数形式？', '["A. cats","B. dogs","C. childs","D. birds"]', 'C', 'child的复数是children，不是childs', 3, '69', 1, 85);

-- 添加父题目ID字段（支持主子两层）
ALTER TABLE t_question ADD COLUMN IF NOT EXISTS parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父题目ID，0表示顶层题目';
CREATE INDEX IF NOT EXISTS idx_question_parent ON t_question(parent_id);

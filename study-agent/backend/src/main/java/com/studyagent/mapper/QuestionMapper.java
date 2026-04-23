package com.studyagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyagent.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    @Select("SELECT * FROM t_question WHERE subject_id = #{subjectId} AND del = 0 ORDER BY id")
    List<Question> selectBySubjectId(@Param("subjectId") Long subjectId);

    Question selectRandomQuestion(@Param("subjectId") Long subjectId, @Param("excludeIds") List<Long> excludeIds);
}

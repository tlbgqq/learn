package com.studyagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyagent.entity.DailyTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DailyTaskMapper extends BaseMapper<DailyTask> {
}

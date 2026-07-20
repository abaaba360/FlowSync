package edu.ustb.flowsync.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.ustb.flowsync.entity.TaskSummary;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskSummaryMapper extends BaseMapper<TaskSummary> {
}

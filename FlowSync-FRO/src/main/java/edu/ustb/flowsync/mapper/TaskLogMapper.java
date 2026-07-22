package edu.ustb.flowsync.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.ustb.flowsync.entity.TaskLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskLogMapper extends BaseMapper<TaskLog> {
}

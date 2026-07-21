package edu.ustb.flowsync.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.ustb.flowsync.entity.TaskLog;
import edu.ustb.flowsync.mapper.TaskLogMapper;
import edu.ustb.flowsync.service.TaskLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskLogServiceImpl implements TaskLogService {
    @Autowired
    private TaskLogMapper taskLogMapper;

    @Override
    public boolean add(TaskLog taskLog) {
        return taskLogMapper.insert(taskLog) > 0;
    }

    @Override
    public List<TaskLog> findList(Long taskId) {
        QueryWrapper<TaskLog> wrapper = new QueryWrapper<>();
        if (taskId != null) {
            wrapper.eq("task_id", taskId);
        }
        wrapper.orderByDesc("id");
        return taskLogMapper.selectList(wrapper);
    }
}

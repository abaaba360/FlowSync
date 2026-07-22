package edu.ustb.flowsync.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.ustb.flowsync.entity.TaskInfo;
import edu.ustb.flowsync.entity.TaskLog;
import edu.ustb.flowsync.entity.User;
import edu.ustb.flowsync.mapper.TaskInfoMapper;
import edu.ustb.flowsync.mapper.TaskLogMapper;
import edu.ustb.flowsync.mapper.UserMapper;
import edu.ustb.flowsync.service.TaskLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskLogServiceImpl implements TaskLogService {
    @Autowired
    private TaskLogMapper taskLogMapper;

    @Autowired
    private TaskInfoMapper taskInfoMapper;

    @Autowired
    private UserMapper userMapper;

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
        wrapper.orderByAsc("id");
        List<TaskLog> list = taskLogMapper.selectList(wrapper);
        for (TaskLog log : list) {
            if (log.getTaskId() != null) {
                TaskInfo t = taskInfoMapper.selectById(log.getTaskId());
                if (t != null) log.setTaskTitle(t.getTitle());
            }
            if (log.getOperatorId() != null) {
                User u = userMapper.selectById(log.getOperatorId());
                if (u != null) log.setOperatorName(u.getRealName());
            }
        }
        return list;
    }
}

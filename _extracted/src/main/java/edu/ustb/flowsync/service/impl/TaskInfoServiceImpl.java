package edu.ustb.flowsync.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.ustb.flowsync.entity.TaskInfo;
import edu.ustb.flowsync.mapper.TaskInfoMapper;
import edu.ustb.flowsync.service.TaskInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskInfoServiceImpl implements TaskInfoService {
    @Autowired
    private TaskInfoMapper taskInfoMapper;

    @Override
    public boolean saveTask(TaskInfo taskInfo, Long currentUserId) {
        if (taskInfo.getId() == null) {
            taskInfo.setCreatorId(currentUserId);
            return taskInfoMapper.insert(taskInfo) > 0;
        }
        return taskInfoMapper.updateById(taskInfo) > 0;
    }

    @Override
    public boolean updateStatus(Long id, String status) {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setId(id);
        taskInfo.setStatus(status);
        return taskInfoMapper.updateById(taskInfo) > 0;
    }

    @Override
    public boolean delById(Long id) {
        return taskInfoMapper.deleteById(id) > 0;
    }

    @Override
    public TaskInfo findById(Long id) {
        return taskInfoMapper.selectById(id);
    }

    @Override
    public List<TaskInfo> findList(Long projectId) {
        QueryWrapper<TaskInfo> wrapper = new QueryWrapper<>();
        if (projectId != null) {
            wrapper.eq("project_id", projectId);
        }
        wrapper.orderByAsc("id");
        return taskInfoMapper.selectList(wrapper);
    }
}

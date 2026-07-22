package edu.ustb.flowsync.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.ustb.flowsync.entity.ProjectInfo;
import edu.ustb.flowsync.entity.TaskInfo;
import edu.ustb.flowsync.entity.TaskSummary;
import edu.ustb.flowsync.entity.User;
import edu.ustb.flowsync.mapper.ProjectInfoMapper;
import edu.ustb.flowsync.mapper.TaskInfoMapper;
import edu.ustb.flowsync.mapper.TaskSummaryMapper;
import edu.ustb.flowsync.mapper.UserMapper;
import edu.ustb.flowsync.service.TaskSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskSummaryServiceImpl implements TaskSummaryService {
    @Autowired
    private TaskSummaryMapper taskSummaryMapper;

    @Autowired
    private ProjectInfoMapper projectInfoMapper;

    @Autowired
    private TaskInfoMapper taskInfoMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean add(TaskSummary taskSummary) {
        return taskSummaryMapper.insert(taskSummary) > 0;
    }

    @Override
    public List<TaskSummary> findList() {
        QueryWrapper<TaskSummary> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("id");
        List<TaskSummary> list = taskSummaryMapper.selectList(wrapper);
        for (TaskSummary s : list) {
            if (s.getProjectId() != null) {
                ProjectInfo p = projectInfoMapper.selectById(s.getProjectId());
                if (p != null) s.setProjectName(p.getName());
            }
            if (s.getTaskId() != null) {
                TaskInfo t = taskInfoMapper.selectById(s.getTaskId());
                if (t != null) s.setTaskTitle(t.getTitle());
            }
            if (s.getCreatedBy() != null) {
                User u = userMapper.selectById(s.getCreatedBy());
                if (u != null) s.setCreatorName(u.getRealName());
            }
        }
        return list;
    }
}

package edu.ustb.flowsync.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.ustb.flowsync.auth.AuthContext;
import edu.ustb.flowsync.entity.ProjectInfo;
import edu.ustb.flowsync.entity.TaskInfo;
import edu.ustb.flowsync.entity.User;
import edu.ustb.flowsync.mapper.ProjectInfoMapper;
import edu.ustb.flowsync.mapper.TaskInfoMapper;
import edu.ustb.flowsync.mapper.UserMapper;
import edu.ustb.flowsync.service.PermissionService;
import edu.ustb.flowsync.service.TaskInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskInfoServiceImpl implements TaskInfoService {
    @Autowired
    private TaskInfoMapper taskInfoMapper;

    @Autowired
    private ProjectInfoMapper projectInfoMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PermissionService permissionService;

    @Override
    public boolean saveTask(TaskInfo taskInfo, Long currentUserId) {
        if (taskInfo.getId() == null) {
            permissionService.requireTaskManagerForProject(taskInfo.getProjectId());
            taskInfo.setCreatorId(AuthContext.getRequiredCurrentUser().getId());
            return taskInfoMapper.insert(taskInfo) > 0;
        }
        permissionService.requireTaskManager(taskInfo.getId());
        // 防止前端篡改归属信息
        taskInfo.setProjectId(null);
        taskInfo.setCreatorId(null);
        taskInfo.setParentId(null);
        taskInfo.setAiSuggestion(null);
        return taskInfoMapper.updateById(taskInfo) > 0;
    }

    @Override
    public boolean updateStatus(Long id, String status) {
        permissionService.requireTaskStatusUpdater(id);
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setId(id);
        taskInfo.setStatus(status);
        return taskInfoMapper.updateById(taskInfo) > 0;
    }

    @Override
    public boolean delById(Long id) {
        permissionService.requireTaskManager(id);
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
        List<TaskInfo> list = taskInfoMapper.selectList(wrapper);
        // 填充展示字段
        for (TaskInfo t : list) {
            if (t.getProjectId() != null) {
                ProjectInfo p = projectInfoMapper.selectById(t.getProjectId());
                if (p != null) t.setProjectName(p.getName());
            }
            if (t.getParentId() != null) {
                TaskInfo parent = taskInfoMapper.selectById(t.getParentId());
                if (parent != null) t.setParentTitle(parent.getTitle());
            }
            if (t.getAssigneeId() != null) {
                User u = userMapper.selectById(t.getAssigneeId());
                if (u != null) t.setAssigneeName(u.getRealName());
            }
            if (t.getCreatorId() != null) {
                User u = userMapper.selectById(t.getCreatorId());
                if (u != null) t.setCreatorName(u.getRealName());
            }
        }
        return list;
    }
}

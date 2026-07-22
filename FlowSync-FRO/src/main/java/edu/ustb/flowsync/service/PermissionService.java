package edu.ustb.flowsync.service;

import edu.ustb.flowsync.auth.AuthContext;
import edu.ustb.flowsync.entity.ProjectInfo;
import edu.ustb.flowsync.entity.TaskInfo;
import edu.ustb.flowsync.entity.User;
import edu.ustb.flowsync.exception.BusinessException;
import edu.ustb.flowsync.mapper.ProjectInfoMapper;
import edu.ustb.flowsync.mapper.TaskInfoMapper;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class PermissionService {
    private static final String ROLE_ADMIN = "管理员";
    private static final String ROLE_PROJECT_LEADER = "项目负责人";
    private static final String ROLE_LEADER = "负责人";

    private final ProjectInfoMapper projectInfoMapper;
    private final TaskInfoMapper taskInfoMapper;

    public PermissionService(ProjectInfoMapper projectInfoMapper, TaskInfoMapper taskInfoMapper) {
        this.projectInfoMapper = projectInfoMapper;
        this.taskInfoMapper = taskInfoMapper;
    }

    public void requireProjectCreatorRole() {
        User user = AuthContext.getRequiredCurrentUser();
        if (isAdmin(user) || isProjectLeader(user)) {
            return;
        }
        throwForbidden();
    }

    public void requireProjectOwner(Long projectId) {
        User user = AuthContext.getRequiredCurrentUser();
        if (isAdmin(user)) {
            return;
        }
        ProjectInfo project = findProject(projectId);
        if (isProjectLeader(user) && Objects.equals(project.getOwnerId(), user.getId())) {
            return;
        }
        throwForbidden();
    }

    public void requireTaskManager(Long taskId) {
        User user = AuthContext.getRequiredCurrentUser();
        if (isAdmin(user)) {
            return;
        }
        TaskInfo task = findTask(taskId);
        ProjectInfo project = findProject(task.getProjectId());
        if (isProjectLeader(user) && Objects.equals(project.getOwnerId(), user.getId())) {
            return;
        }
        throwForbidden();
    }

    public void requireTaskManagerForProject(Long projectId) {
        requireProjectOwner(projectId);
    }

    public void requireTaskStatusUpdater(Long taskId) {
        User user = AuthContext.getRequiredCurrentUser();
        if (isAdmin(user)) {
            return;
        }
        TaskInfo task = findTask(taskId);
        if (Objects.equals(task.getAssigneeId(), user.getId())) {
            return;
        }
        ProjectInfo project = findProject(task.getProjectId());
        if (isProjectLeader(user) && Objects.equals(project.getOwnerId(), user.getId())) {
            return;
        }
        throwForbidden();
    }

    public static boolean isAdmin(User user) {
        return user != null && ROLE_ADMIN.equals(user.getRole());
    }

    private static boolean isProjectLeader(User user) {
        return user != null && (ROLE_PROJECT_LEADER.equals(user.getRole()) || ROLE_LEADER.equals(user.getRole()));
    }

    private ProjectInfo findProject(Long projectId) {
        if (projectId == null) {
            throw new BusinessException(400, "项目ID不能为空");
        }
        ProjectInfo project = projectInfoMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(404, "项目不存在");
        }
        return project;
    }

    private TaskInfo findTask(Long taskId) {
        if (taskId == null) {
            throw new BusinessException(400, "任务ID不能为空");
        }
        TaskInfo task = taskInfoMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }
        return task;
    }

    private void throwForbidden() {
        throw new BusinessException(403, "无权限操作");
    }
}

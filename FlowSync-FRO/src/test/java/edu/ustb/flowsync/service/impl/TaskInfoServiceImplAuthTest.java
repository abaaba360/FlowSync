package edu.ustb.flowsync.service.impl;

import edu.ustb.flowsync.auth.AuthContext;
import edu.ustb.flowsync.entity.ProjectInfo;
import edu.ustb.flowsync.entity.TaskInfo;
import edu.ustb.flowsync.entity.User;
import edu.ustb.flowsync.exception.BusinessException;
import edu.ustb.flowsync.mapper.ProjectInfoMapper;
import edu.ustb.flowsync.mapper.TaskInfoMapper;
import edu.ustb.flowsync.service.PermissionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TaskInfoServiceImplAuthTest {

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    void projectOwnerCanCreateTaskForProject() {
        TaskInfoMapper taskInfoMapper = mock(TaskInfoMapper.class);
        ProjectInfoMapper projectInfoMapper = mock(ProjectInfoMapper.class);
        when(taskInfoMapper.insert(any(TaskInfo.class))).thenReturn(1);
        when(projectInfoMapper.selectById(10L)).thenReturn(project(10L, 1L));
        TaskInfoServiceImpl service = service(taskInfoMapper, projectInfoMapper);

        AuthContext.setCurrentUser(user(1L, "项目负责人"));
        TaskInfo task = new TaskInfo();
        task.setProjectId(10L);

        service.saveTask(task, 888L);

        assertEquals(1L, task.getCreatorId());
    }

    @Test
    void memberCannotEditTaskEvenWhenAssigned() {
        TaskInfoMapper taskInfoMapper = mock(TaskInfoMapper.class);
        ProjectInfoMapper projectInfoMapper = mock(ProjectInfoMapper.class);
        when(taskInfoMapper.selectById(20L)).thenReturn(task(20L, 10L, 2L));
        when(projectInfoMapper.selectById(10L)).thenReturn(project(10L, 1L));
        TaskInfoServiceImpl service = service(taskInfoMapper, projectInfoMapper);

        AuthContext.setCurrentUser(user(2L, "成员"));
        TaskInfo task = new TaskInfo();
        task.setId(20L);
        task.setProjectId(10L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.saveTask(task, 2L)
        );
        assertEquals("无权限操作", exception.getMsg());
    }

    @Test
    void assigneeCanUpdateOwnTaskStatus() {
        TaskInfoMapper taskInfoMapper = mock(TaskInfoMapper.class);
        ProjectInfoMapper projectInfoMapper = mock(ProjectInfoMapper.class);
        when(taskInfoMapper.selectById(20L)).thenReturn(task(20L, 10L, 2L));
        when(projectInfoMapper.selectById(10L)).thenReturn(project(10L, 1L));
        when(taskInfoMapper.updateById(any(TaskInfo.class))).thenReturn(1);
        TaskInfoServiceImpl service = service(taskInfoMapper, projectInfoMapper);

        AuthContext.setCurrentUser(user(2L, "成员"));

        service.updateStatus(20L, "进行中");
    }

    private TaskInfoServiceImpl service(TaskInfoMapper taskInfoMapper, ProjectInfoMapper projectInfoMapper) {
        TaskInfoServiceImpl service = new TaskInfoServiceImpl();
        ReflectionTestUtils.setField(service, "taskInfoMapper", taskInfoMapper);
        ReflectionTestUtils.setField(service, "permissionService", new PermissionService(projectInfoMapper, taskInfoMapper));
        return service;
    }

    private ProjectInfo project(Long id, Long ownerId) {
        ProjectInfo project = new ProjectInfo();
        project.setId(id);
        project.setOwnerId(ownerId);
        return project;
    }

    private TaskInfo task(Long id, Long projectId, Long assigneeId) {
        TaskInfo task = new TaskInfo();
        task.setId(id);
        task.setProjectId(projectId);
        task.setAssigneeId(assigneeId);
        return task;
    }

    private User user(Long id, String role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}

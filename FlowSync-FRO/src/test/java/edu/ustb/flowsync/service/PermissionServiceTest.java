package edu.ustb.flowsync.service;

import edu.ustb.flowsync.auth.AuthContext;
import edu.ustb.flowsync.entity.ProjectInfo;
import edu.ustb.flowsync.entity.TaskInfo;
import edu.ustb.flowsync.entity.User;
import edu.ustb.flowsync.exception.BusinessException;
import edu.ustb.flowsync.mapper.ProjectInfoMapper;
import edu.ustb.flowsync.mapper.TaskInfoMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PermissionServiceTest {

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    void projectOwnerCanManageProjectButOtherMemberCannot() {
        ProjectInfoMapper projectInfoMapper = mock(ProjectInfoMapper.class);
        TaskInfoMapper taskInfoMapper = mock(TaskInfoMapper.class);
        PermissionService permissionService = new PermissionService(projectInfoMapper, taskInfoMapper);

        ProjectInfo project = new ProjectInfo();
        project.setId(10L);
        project.setOwnerId(1L);
        when(projectInfoMapper.selectById(10L)).thenReturn(project);

        AuthContext.setCurrentUser(user(1L, "负责人"));
        assertDoesNotThrow(() -> permissionService.requireProjectOwner(10L));

        AuthContext.setCurrentUser(user(1L, "成员"));
        BusinessException ownerButMemberException = assertThrows(
                BusinessException.class,
                () -> permissionService.requireProjectOwner(10L)
        );
        assertEquals("无权限操作", ownerButMemberException.getMsg());

        AuthContext.setCurrentUser(user(2L, "成员"));
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> permissionService.requireProjectOwner(10L)
        );
        assertEquals("无权限操作", exception.getMsg());
    }

    @Test
    void memberCannotManageTaskEvenWhenProjectOwnerIdMatches() {
        ProjectInfoMapper projectInfoMapper = mock(ProjectInfoMapper.class);
        TaskInfoMapper taskInfoMapper = mock(TaskInfoMapper.class);
        PermissionService permissionService = new PermissionService(projectInfoMapper, taskInfoMapper);

        ProjectInfo project = new ProjectInfo();
        project.setId(10L);
        project.setOwnerId(2L);
        TaskInfo task = new TaskInfo();
        task.setId(20L);
        task.setProjectId(10L);
        task.setAssigneeId(3L);
        when(projectInfoMapper.selectById(10L)).thenReturn(project);
        when(taskInfoMapper.selectById(20L)).thenReturn(task);

        AuthContext.setCurrentUser(user(2L, "成员"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> permissionService.requireTaskManager(20L)
        );
        assertEquals("无权限操作", exception.getMsg());
    }

    @Test
    void assigneeCanUpdateTaskStatusButCannotManageTask() {
        ProjectInfoMapper projectInfoMapper = mock(ProjectInfoMapper.class);
        TaskInfoMapper taskInfoMapper = mock(TaskInfoMapper.class);
        PermissionService permissionService = new PermissionService(projectInfoMapper, taskInfoMapper);

        ProjectInfo project = new ProjectInfo();
        project.setId(10L);
        project.setOwnerId(1L);
        TaskInfo task = new TaskInfo();
        task.setId(20L);
        task.setProjectId(10L);
        task.setAssigneeId(2L);
        when(projectInfoMapper.selectById(10L)).thenReturn(project);
        when(taskInfoMapper.selectById(20L)).thenReturn(task);

        AuthContext.setCurrentUser(user(2L, "成员"));
        assertDoesNotThrow(() -> permissionService.requireTaskStatusUpdater(20L));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> permissionService.requireTaskManager(20L)
        );
        assertEquals("无权限操作", exception.getMsg());
    }

    @Test
    void adminCanManageAnyProjectAndTask() {
        ProjectInfoMapper projectInfoMapper = mock(ProjectInfoMapper.class);
        TaskInfoMapper taskInfoMapper = mock(TaskInfoMapper.class);
        PermissionService permissionService = new PermissionService(projectInfoMapper, taskInfoMapper);

        AuthContext.setCurrentUser(user(99L, "管理员"));

        assertDoesNotThrow(() -> permissionService.requireProjectOwner(10L));
        assertDoesNotThrow(() -> permissionService.requireTaskManager(20L));
        assertDoesNotThrow(() -> permissionService.requireTaskStatusUpdater(20L));
    }

    private User user(Long id, String role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}

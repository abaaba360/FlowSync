package edu.ustb.flowsync.service.impl;

import edu.ustb.flowsync.auth.AuthContext;
import edu.ustb.flowsync.entity.ProjectInfo;
import edu.ustb.flowsync.entity.User;
import edu.ustb.flowsync.exception.BusinessException;
import edu.ustb.flowsync.mapper.ProjectInfoMapper;
import edu.ustb.flowsync.mapper.TaskInfoMapper;
import edu.ustb.flowsync.service.PermissionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectInfoServiceImplAuthTest {

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    void createProjectUsesAuthenticatedUserAsOwnerInsteadOfRequestParameter() {
        ProjectInfoMapper projectInfoMapper = mock(ProjectInfoMapper.class);
        when(projectInfoMapper.insert(any(ProjectInfo.class))).thenReturn(1);
        ProjectInfoServiceImpl service = new ProjectInfoServiceImpl();
        ReflectionTestUtils.setField(service, "projectInfoMapper", projectInfoMapper);
        ReflectionTestUtils.setField(service, "permissionService", new PermissionService(projectInfoMapper, mock(TaskInfoMapper.class)));

        AuthContext.setCurrentUser(user(7L, "项目负责人"));
        ProjectInfo project = new ProjectInfo();
        project.setName("安全改造");

        service.saveProject(project, 999L);

        ArgumentCaptor<ProjectInfo> captor = ArgumentCaptor.forClass(ProjectInfo.class);
        verify(projectInfoMapper).insert(captor.capture());
        assertEquals(7L, captor.getValue().getOwnerId());
    }

    @Test
    void memberCannotCreateProject() {
        ProjectInfoServiceImpl service = new ProjectInfoServiceImpl();
        ProjectInfoMapper projectInfoMapper = mock(ProjectInfoMapper.class);
        ReflectionTestUtils.setField(service, "projectInfoMapper", projectInfoMapper);
        ReflectionTestUtils.setField(service, "permissionService", new PermissionService(projectInfoMapper, mock(TaskInfoMapper.class)));

        AuthContext.setCurrentUser(user(2L, "成员"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.saveProject(new ProjectInfo(), 2L)
        );
        assertEquals("无权限操作", exception.getMsg());
    }

    private User user(Long id, String role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}

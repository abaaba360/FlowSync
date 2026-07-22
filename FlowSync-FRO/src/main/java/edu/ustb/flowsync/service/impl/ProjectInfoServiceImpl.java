package edu.ustb.flowsync.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.ustb.flowsync.auth.AuthContext;
import edu.ustb.flowsync.entity.ProjectInfo;
import edu.ustb.flowsync.entity.User;
import edu.ustb.flowsync.mapper.ProjectInfoMapper;
import edu.ustb.flowsync.mapper.UserMapper;
import edu.ustb.flowsync.service.PermissionService;
import edu.ustb.flowsync.service.ProjectInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectInfoServiceImpl implements ProjectInfoService {
    @Autowired
    private ProjectInfoMapper projectInfoMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PermissionService permissionService;

    @Override
    public boolean saveProject(ProjectInfo projectInfo, Long currentUserId) {
        if (projectInfo.getId() == null) {
            permissionService.requireProjectCreatorRole();
            projectInfo.setOwnerId(AuthContext.getRequiredCurrentUser().getId());
            return projectInfoMapper.insert(projectInfo) > 0;
        }
        permissionService.requireProjectOwner(projectInfo.getId());
        projectInfo.setOwnerId(null);
        return projectInfoMapper.updateById(projectInfo) > 0;
    }

    @Override
    public boolean delById(Long id) {
        permissionService.requireProjectOwner(id);
        return projectInfoMapper.deleteById(id) > 0;
    }

    @Override
    public ProjectInfo findById(Long id) {
        return projectInfoMapper.selectById(id);
    }

    @Override
    public List<ProjectInfo> findList() {
        QueryWrapper<ProjectInfo> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("id");
        List<ProjectInfo> list = projectInfoMapper.selectList(wrapper);
        for (ProjectInfo p : list) {
            if (p.getOwnerId() != null) {
                User u = userMapper.selectById(p.getOwnerId());
                if (u != null) p.setOwnerName(u.getRealName());
            }
        }
        return list;
    }
}

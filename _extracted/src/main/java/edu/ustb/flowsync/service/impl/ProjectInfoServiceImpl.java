package edu.ustb.flowsync.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.ustb.flowsync.entity.ProjectInfo;
import edu.ustb.flowsync.mapper.ProjectInfoMapper;
import edu.ustb.flowsync.service.ProjectInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectInfoServiceImpl implements ProjectInfoService {
    @Autowired
    private ProjectInfoMapper projectInfoMapper;

    @Override
    public boolean saveProject(ProjectInfo projectInfo, Long currentUserId) {
        if (projectInfo.getId() == null) {
            if (projectInfo.getOwnerId() == null) {
                projectInfo.setOwnerId(currentUserId);
            }
            return projectInfoMapper.insert(projectInfo) > 0;
        }
        return projectInfoMapper.updateById(projectInfo) > 0;
    }

    @Override
    public boolean delById(Long id) {
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
        return projectInfoMapper.selectList(wrapper);
    }
}

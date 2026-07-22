package edu.ustb.flowsync.service;

import edu.ustb.flowsync.entity.ProjectInfo;

import java.util.List;

public interface ProjectInfoService {
    boolean saveProject(ProjectInfo projectInfo, Long currentUserId);

    boolean delById(Long id);

    ProjectInfo findById(Long id);

    List<ProjectInfo> findList();
}

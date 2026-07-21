package edu.ustb.flowsync.service;

import edu.ustb.flowsync.entity.TaskInfo;

import java.util.List;

public interface TaskInfoService {
    boolean saveTask(TaskInfo taskInfo, Long currentUserId);

    boolean updateStatus(Long id, String status);

    boolean delById(Long id);

    TaskInfo findById(Long id);

    List<TaskInfo> findList(Long projectId);
}

package edu.ustb.flowsync.service;

import edu.ustb.flowsync.entity.TaskLog;

import java.util.List;

public interface TaskLogService {
    boolean add(TaskLog taskLog);

    List<TaskLog> findList(Long taskId);
}

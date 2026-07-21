package edu.ustb.flowsync.service;

import edu.ustb.flowsync.entity.TaskSummary;

import java.util.List;

public interface TaskSummaryService {
    boolean add(TaskSummary taskSummary);

    List<TaskSummary> findList();
}

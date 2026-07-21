package edu.ustb.flowsync.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.ustb.flowsync.entity.TaskSummary;
import edu.ustb.flowsync.mapper.TaskSummaryMapper;
import edu.ustb.flowsync.service.TaskSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskSummaryServiceImpl implements TaskSummaryService {
    @Autowired
    private TaskSummaryMapper taskSummaryMapper;

    @Override
    public boolean add(TaskSummary taskSummary) {
        return taskSummaryMapper.insert(taskSummary) > 0;
    }

    @Override
    public List<TaskSummary> findList() {
        QueryWrapper<TaskSummary> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("id");
        return taskSummaryMapper.selectList(wrapper);
    }
}

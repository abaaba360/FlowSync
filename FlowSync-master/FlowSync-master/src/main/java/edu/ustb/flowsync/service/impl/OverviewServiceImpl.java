package edu.ustb.flowsync.service.impl;

import edu.ustb.flowsync.dto.OverviewData;
import edu.ustb.flowsync.mapper.ProjectInfoMapper;
import edu.ustb.flowsync.mapper.TaskInfoMapper;
import edu.ustb.flowsync.mapper.TaskSummaryMapper;
import edu.ustb.flowsync.mapper.UserMapper;
import edu.ustb.flowsync.service.OverviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OverviewServiceImpl implements OverviewService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProjectInfoMapper projectInfoMapper;

    @Autowired
    private TaskInfoMapper taskInfoMapper;

    @Autowired
    private TaskSummaryMapper taskSummaryMapper;

    @Override
    public OverviewData overview() {
        return new OverviewData(
                userMapper.selectCount(null),
                projectInfoMapper.selectCount(null),
                taskInfoMapper.selectCount(null),
                taskSummaryMapper.selectCount(null)
        );
    }
}

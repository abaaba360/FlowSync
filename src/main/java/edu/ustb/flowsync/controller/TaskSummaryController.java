package edu.ustb.flowsync.controller;

import edu.ustb.flowsync.common.ApiResponse;
import edu.ustb.flowsync.entity.TaskSummary;
import edu.ustb.flowsync.service.TaskSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/summaries")
@CrossOrigin
public class TaskSummaryController {
    @Autowired
    private TaskSummaryService taskSummaryService;

    @GetMapping
    public ApiResponse<List<TaskSummary>> list() {
        return ApiResponse.success("查询总结列表成功", taskSummaryService.findList());
    }

    @PostMapping
    public ApiResponse<Object> add(@RequestBody TaskSummary taskSummary) {
        if (taskSummaryService.add(taskSummary)) {
            return ApiResponse.success("新增总结成功");
        }
        return ApiResponse.fail("新增总结失败");
    }
}

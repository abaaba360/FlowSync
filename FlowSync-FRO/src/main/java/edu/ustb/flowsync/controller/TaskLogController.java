package edu.ustb.flowsync.controller;

import edu.ustb.flowsync.common.ApiResponse;
import edu.ustb.flowsync.entity.TaskLog;
import edu.ustb.flowsync.service.TaskLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/task-logs")
@CrossOrigin
public class TaskLogController {
    @Autowired
    private TaskLogService taskLogService;

    @GetMapping
    public ApiResponse<List<TaskLog>> list(@RequestParam(value = "taskId", required = false) Long taskId) {
        return ApiResponse.success("查询进度记录成功", taskLogService.findList(taskId));
    }

    @PostMapping
    public ApiResponse<Object> add(@RequestBody TaskLog taskLog) {
        if (taskLogService.add(taskLog)) {
            return ApiResponse.success("新增进度记录成功");
        }
        return ApiResponse.fail("新增进度记录失败");
    }
}

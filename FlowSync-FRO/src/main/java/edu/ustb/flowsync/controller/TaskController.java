package edu.ustb.flowsync.controller;

import edu.ustb.flowsync.common.ApiResponse;
import edu.ustb.flowsync.dto.TaskStatusUpdateRequest;
import edu.ustb.flowsync.entity.TaskInfo;
import edu.ustb.flowsync.service.TaskInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin
public class TaskController {
    @Autowired
    private TaskInfoService taskInfoService;

    @GetMapping
    public ApiResponse<List<TaskInfo>> list(@RequestParam(value = "projectId", required = false) Long projectId) {
        return ApiResponse.success("查询任务列表成功", taskInfoService.findList(projectId));
    }

    @GetMapping("/{id}")
    public ApiResponse<TaskInfo> one(@PathVariable("id") Long id) {
        return ApiResponse.success("查询任务成功", taskInfoService.findById(id));
    }

    @PostMapping
    public ApiResponse<Object> save(@RequestBody TaskInfo taskInfo,
                                    @RequestParam(value = "currentUserId", required = false) Long currentUserId) {
        if (taskInfoService.saveTask(taskInfo, currentUserId)) {
            return ApiResponse.success("保存任务成功");
        }
        return ApiResponse.fail("保存任务失败");
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Object> updateStatus(@PathVariable("id") Long id,
                                            @RequestBody TaskStatusUpdateRequest request) {
        if (taskInfoService.updateStatus(id, request.getStatus())) {
            return ApiResponse.success("更新任务状态成功");
        }
        return ApiResponse.fail("更新任务状态失败");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Object> delete(@PathVariable("id") Long id) {
        if (taskInfoService.delById(id)) {
            return ApiResponse.success("删除任务成功");
        }
        return ApiResponse.fail("删除任务失败");
    }
}

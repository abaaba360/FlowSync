package edu.ustb.flowsync.controller;

import edu.ustb.flowsync.common.ApiResponse;
import edu.ustb.flowsync.entity.ProjectInfo;
import edu.ustb.flowsync.service.ProjectInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin
public class ProjectController {
    @Autowired
    private ProjectInfoService projectInfoService;

    @GetMapping
    public ApiResponse<List<ProjectInfo>> list() {
        return ApiResponse.success("查询项目列表成功", projectInfoService.findList());
    }

    @GetMapping("/{id}")
    public ApiResponse<ProjectInfo> one(@PathVariable("id") Long id) {
        return ApiResponse.success("查询项目成功", projectInfoService.findById(id));
    }

    @PostMapping
    public ApiResponse<Object> save(@RequestBody ProjectInfo projectInfo,
                                    @RequestParam(value = "currentUserId", required = false) Long currentUserId) {
        if (projectInfoService.saveProject(projectInfo, currentUserId)) {
            return ApiResponse.success("保存项目成功");
        }
        return ApiResponse.fail("保存项目失败");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Object> delete(@PathVariable("id") Long id) {
        if (projectInfoService.delById(id)) {
            return ApiResponse.success("删除项目成功");
        }
        return ApiResponse.fail("删除项目失败");
    }
}

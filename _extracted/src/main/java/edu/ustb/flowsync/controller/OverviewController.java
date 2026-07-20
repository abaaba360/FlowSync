package edu.ustb.flowsync.controller;

import edu.ustb.flowsync.common.ApiResponse;
import edu.ustb.flowsync.dto.OverviewData;
import edu.ustb.flowsync.service.OverviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/overview")
@CrossOrigin
public class OverviewController {
    @Autowired
    private OverviewService overviewService;

    @GetMapping
    public ApiResponse<OverviewData> overview() {
        return ApiResponse.success("查询总览成功", overviewService.overview());
    }
}

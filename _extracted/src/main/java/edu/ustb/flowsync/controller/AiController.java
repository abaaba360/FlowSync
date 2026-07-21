package edu.ustb.flowsync.controller;

import edu.ustb.flowsync.common.ApiResponse;
import edu.ustb.flowsync.dto.AiTaskGenerateRequest;
import edu.ustb.flowsync.dto.AiTaskResponse;
import edu.ustb.flowsync.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI智能拆解控制器
 * 对应规格说明书 6.3 AI智能拆解（任务生成）
 * <p>
 * 接口设计遵循"AI只做建议，不做决策"原则：
 * 后端生成任务草稿后返回前端，由用户在弹窗中确认、调整后再手动导入到任务表。
 * </p>
 *
 * @see AiService
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AiController {

    @Autowired
    private AiService aiService;

    /**
     * AI智能拆解——生成项目任务
     * 对应规格说明书 6.3.2 接口定义
     * <p>
     * 请求体包含项目名称、目标、描述和可选成员列表，
     * 返回AI生成的拆解概要 + 任务列表（含推荐负责人）。
     * 若AI调用失败，返回四阶段兜底方案。
     * </p>
     *
     * @param request 项目信息和可选成员列表
     * @return AI生成的任务拆解结果
     */
    @PostMapping("/generate-tasks")
    public ApiResponse<AiTaskResponse> generateTasks(@RequestBody AiTaskGenerateRequest request) {
        System.out.println("[AiController] generateTasks() 被调用...");
        System.out.println("[AiController] 项目名称: " + request.getProjectName());
        System.out.println("[AiController] 成员数量: " +
                (request.getMembers() != null ? request.getMembers().size() : 0));

        AiTaskResponse result = aiService.generateTasks(request);

        if (result.isFallback()) {
            return ApiResponse.success("AI服务暂不可用，已返回兜底方案，请手动调整", result);
        }
        return ApiResponse.success("AI任务拆解完成，请确认后导入", result);
    }
}

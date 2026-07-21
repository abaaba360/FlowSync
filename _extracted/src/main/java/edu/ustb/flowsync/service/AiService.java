package edu.ustb.flowsync.service;

import edu.ustb.flowsync.dto.AiTaskGenerateRequest;
import edu.ustb.flowsync.dto.AiTaskResponse;

/**
 * AI服务接口
 * 对应规格说明书 6. AI功能设计
 */
public interface AiService {
    /**
     * 调用AI大模型拆解项目任务
     * @param request 包含项目信息和可选成员列表
     * @return AI生成的任务列表 + 概要
     */
    AiTaskResponse generateTasks(AiTaskGenerateRequest request);
}

package edu.ustb.flowsync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI任务拆解请求DTO
 * 对应规格说明书 6.3 AI智能拆解——接口输入
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiTaskGenerateRequest {
    /** 项目ID */
    private Long projectId;
    /** 操作人ID（前端传入，当前登录用户） */
    private Long operatorId;
    /** 项目名称 */
    private String projectName;
    /** 项目目标 */
    private String goal;
    /** 项目说明/补充描述 */
    private String description;
    /** 可选成员列表（后端自动查询填充，前端无需传） */
    private List<MemberInfo> members;
}

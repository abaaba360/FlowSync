package edu.ustb.flowsync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI生成的任务项
 * 对应规格说明书 6.3.6 JSON返回格式中的 items 数组元素
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiTaskItem {
    /** 任务标题 */
    private String title;
    /** 任务描述（做什么、交付什么） */
    private String description;
    /** 优先级：高/中/低 */
    private String priority;
    /** 建议完成天数 */
    private Integer suggestedDays;
    /** AI推荐的负责人ID（来自成员列表） */
    private Long assigneeId;
    /** 推荐的负责人姓名（后端填充，方便前端展示） */
    private String assigneeName;
}

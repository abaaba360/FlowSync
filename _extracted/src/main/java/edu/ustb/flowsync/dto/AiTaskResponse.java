package edu.ustb.flowsync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI任务拆解响应DTO
 * 对应规格说明书 6.3.6 JSON返回格式
 * {"summary": "...", "items": [{"title": "...", "description": "...", "priority": "中", "suggestedDays": 3, "assigneeId": 1}]}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiTaskResponse {
    /** AI生成的拆解概要/总结 */
    private String summary;
    /** AI生成的任务列表 */
    private List<AiTaskItem> items;
    /** 是否为兜底方案（AI调用失败时的降级方案） */
    private boolean fallback;
}

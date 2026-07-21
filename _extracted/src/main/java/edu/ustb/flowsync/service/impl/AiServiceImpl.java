package edu.ustb.flowsync.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.protocol.Protocol;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ustb.flowsync.dto.AiTaskGenerateRequest;
import edu.ustb.flowsync.dto.AiTaskItem;
import edu.ustb.flowsync.dto.AiTaskResponse;
import edu.ustb.flowsync.dto.MemberInfo;
import edu.ustb.flowsync.service.AiService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI服务实现 —— 接入阿里云千问大模型（DashScope SDK）
 * 对应规格说明书 6. AI功能设计
 * <p>
 * 设计原则：AI只做建议，不做决策——生成结果由负责人确认后手动导入。
 * </p>
 */
@Service
public class AiServiceImpl implements AiService {

    // ==================== 阿里云百炼配置 ====================
    /** 华北2（北京）地域的工作空间URL，请替换为你的工作空间ID */
    private static final String API_BASE_URL =
            "https://{your-workspace-id}.cn-beijing.maas.aliyuncs.com/api/v1";

    /** API Key（阿里云百炼），请替换为你自己的Key */
    private static final String API_KEY = "sk-your-api-key-here";

    /** 模型：千问 Plus */
    private static final String MODEL_NAME = "qwen-plus";

    // ==================== System Prompt ====================
    /**
     * 系统提示词
     * 对应规格说明书 6.3.4 System Prompt 设计
     */
    private static final String SYSTEM_PROMPT =
            "你是一个资深项目管理专家，擅长将复杂项目拆解成可执行的任务单元。\n" +
            "你的工作原则：\n" +
            "1. 遵循MECE原则（相互独立，完全穷尽）\n" +
            "2. 每个任务颗粒度适中——既不能太粗（无法执行），也不能太细（过于繁琐）\n" +
            "3. 任务描述需要具体、可操作，包含明确动作和交付物\n" +
            "4. 你需要将一个项目完整拆分为多个子任务，并为每个任务推荐一个最合适的负责人\n" +
            "5. 为每个任务的 assigneeId 字段填写该成员在成员列表中的 id\n" +
            "6. 每项任务都必须填写 assigneeId，不能为空\n" +
            "7. 同一个人可以负责多个任务\n" +
            "8. 严格按照用户要求的JSON格式进行输出，不添加任何多余的文字、解释或markdown标记\n" +
            "9. 只输出纯JSON，不要用```json```包裹";

    // Jackson ObjectMapper（线程安全，可复用）
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AiTaskResponse generateTasks(AiTaskGenerateRequest request) {
        try {
            // 1. 构建用户提示词
            String userMessage = buildUserPrompt(request);

            // 2. 调用千问模型
            String aiRawResponse = callQwenModel(userMessage);

            // 3. 解析JSON并校验
            AiTaskResponse response = parseAiResponse(aiRawResponse, request.getMembers());

            // 4. 填充负责人姓名
            fillAssigneeNames(response, request.getMembers());

            return response;

        } catch (Exception e) {
            System.err.println("[AiService] AI调用或解析失败，启用兜底方案: " + e.getMessage());
            e.printStackTrace();
            // 对应规格说明书 6.3.7 兜底机制
            return buildFallbackPlan(request);
        }
    }

    // ==================== 构建用户提示词 ====================

    /**
     * 根据请求构建User Prompt
     * 对应规格说明书 6.3.5 User Prompt 模板
     */
    private String buildUserPrompt(AiTaskGenerateRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("请将以下项目拆解为多个可执行任务，并以JSON格式输出。\n\n");

        sb.append("## 项目信息\n");
        sb.append("- 项目名称：").append(request.getProjectName()).append("\n");
        if (request.getGoal() != null && !request.getGoal().isEmpty()) {
            sb.append("- 项目目标：").append(request.getGoal()).append("\n");
        }
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            sb.append("- 补充说明：").append(request.getDescription()).append("\n");
        }

        sb.append("\n## 可选成员列表（id - 姓名 - 角色）\n");
        if (request.getMembers() != null && !request.getMembers().isEmpty()) {
            for (MemberInfo member : request.getMembers()) {
                sb.append(member.getId()).append(" - ").append(member.getName());
                if (member.getRole() != null && !member.getRole().isEmpty()) {
                    sb.append("（").append(member.getRole()).append("）");
                }
                sb.append("\n");
            }
        }

        sb.append("\n## 拆解要求\n");
        sb.append("1. 任务要覆盖项目全流程（包含但不限于：准备阶段、资料收集、执行主体、汇报总结）\n");
        sb.append("2. 每个任务必须包含以下字段，不可遗漏：\n");
        sb.append("   - title：任务标题（简洁明了）\n");
        sb.append("   - description：任务描述（包含：做什么、交付什么）\n");
        sb.append("   - priority：优先级（高/中/低）\n");
        sb.append("   - suggestedDays：建议完成天数（整数）\n");
        sb.append("   - assigneeId：任务负责人ID（从可选成员列表中合理分配）\n");
        sb.append("3. 负责人分配原则：\n");
        sb.append("   - 合理分摊工作量，避免一人承担过多\n");
        sb.append("   - 根据任务性质分配（如：汇报类分配给擅长表达的成员）\n");
        sb.append("4. 每项任务都必须有 assigneeId，不能为空\n");
        sb.append("5. 同时请提供一个 summary 字段，用一句话总结任务拆解思路\n");

        sb.append("\n## 严格遵守JSON输出格式（不要markdown标记）\n");
        sb.append("{\n");
        sb.append("  \"summary\": \"拆解思路总结...\",\n");
        sb.append("  \"items\": [\n");
        sb.append("    {\"title\": \"...\", \"description\": \"...\", \"priority\": \"中\", \"suggestedDays\": 3, \"assigneeId\": 1}\n");
        sb.append("  ]\n");
        sb.append("}\n");

        return sb.toString();
    }

    // ==================== 调用千问模型 ====================

    /**
     * 调用阿里云千问大模型
     * 参考 demo5/Main.java 的 DashScope SDK 调用方式
     */
    private String callQwenModel(String userMessage)
            throws ApiException, NoApiKeyException, InputRequiredException {

        // 构建系统消息
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(SYSTEM_PROMPT)
                .build();

        // 构建用户消息
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(userMessage)
                .build();

        // 创建Generation实例
        Generation gen = new Generation(Protocol.HTTP.getValue(), API_BASE_URL);

        // 构建请求参数
        GenerationParam param = GenerationParam.builder()
                .apiKey(API_KEY)
                .model(MODEL_NAME)
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();

        // 调用并返回
        GenerationResult result = gen.call(param);
        return result.getOutput().getChoices().get(0).getMessage().getContent();
    }

    // ==================== 解析AI响应 ====================

    /**
     * 解析AI返回的JSON
     * 对应规格说明书 6.3.6 JSON返回格式
     * 包含 assigneeId 有效性校验（规格说明书 6.3.8）
     */
    private AiTaskResponse parseAiResponse(String aiRawResponse, List<MemberInfo> members) {
        // 收集有效的成员ID集合，用于校验
        Set<Long> validMemberIds = members != null
                ? members.stream().map(MemberInfo::getId).collect(Collectors.toSet())
                : Set.of();

        // 默认负责人ID：取第一个成员，若没有则用1
        Long defaultAssigneeId = (members != null && !members.isEmpty())
                ? members.get(0).getId() : 1L;

        try {
            // 清理可能存在的markdown代码块标记
            String cleanedJson = aiRawResponse.trim();
            if (cleanedJson.startsWith("```json")) {
                cleanedJson = cleanedJson.substring(7);
            } else if (cleanedJson.startsWith("```")) {
                cleanedJson = cleanedJson.substring(3);
            }
            if (cleanedJson.endsWith("```")) {
                cleanedJson = cleanedJson.substring(0, cleanedJson.length() - 3);
            }
            cleanedJson = cleanedJson.trim();

            // 解析JSON
            JsonNode root = objectMapper.readTree(cleanedJson);

            AiTaskResponse response = new AiTaskResponse();
            response.setSummary(root.has("summary") ? root.get("summary").asText() : "");
            response.setFallback(false);

            List<AiTaskItem> items = new ArrayList<>();
            JsonNode itemsNode = root.get("items");
            if (itemsNode != null && itemsNode.isArray()) {
                for (JsonNode itemNode : itemsNode) {
                    AiTaskItem item = new AiTaskItem();
                    item.setTitle(itemNode.has("title") ? itemNode.get("title").asText() : "");
                    item.setDescription(itemNode.has("description") ? itemNode.get("description").asText() : "");
                    item.setPriority(itemNode.has("priority") ? itemNode.get("priority").asText() : "中");
                    item.setSuggestedDays(itemNode.has("suggestedDays") ? itemNode.get("suggestedDays").asInt() : 3);

                    // 校验 assigneeId 有效性（规格说明书 6.3.8）
                    Long assigneeId = itemNode.has("assigneeId")
                            ? itemNode.get("assigneeId").asLong()
                            : null;
                    if (assigneeId != null && validMemberIds.contains(assigneeId)) {
                        item.setAssigneeId(assigneeId);
                    } else {
                        // 无效或缺失则回退为第一个成员（默认负责人）
                        item.setAssigneeId(defaultAssigneeId);
                    }

                    items.add(item);
                }
            }
            response.setItems(items);
            return response;

        } catch (JsonProcessingException e) {
            System.err.println("[AiService] JSON解析失败: " + e.getMessage());
            throw new RuntimeException("AI返回数据格式异常，启用兜底方案", e);
        }
    }

    // ==================== 填充负责人姓名 ====================

    /**
     * 根据 assigneeId 填充 assigneeName
     */
    private void fillAssigneeNames(AiTaskResponse response, List<MemberInfo> members) {
        if (response.getItems() == null || members == null) return;

        for (AiTaskItem item : response.getItems()) {
            if (item.getAssigneeId() != null) {
                for (MemberInfo member : members) {
                    if (member.getId().equals(item.getAssigneeId())) {
                        item.setAssigneeName(member.getName());
                        break;
                    }
                }
            }
        }
    }

    // ==================== 兜底方案 ====================

    /**
     * AI调用失败时的兜底方案
     * 对应规格说明书 6.3.7 兜底机制
     * 生成四阶段基础任务：准备阶段 → 资料收集 → 执行主体 → 汇报总结
     * 兜底方案不包含 assigneeId，由用户在前端手动选择
     */
    private AiTaskResponse buildFallbackPlan(AiTaskGenerateRequest request) {
        AiTaskResponse response = new AiTaskResponse();
        response.setFallback(true);
        response.setSummary("AI服务暂时不可用，已为您生成基础四阶段任务框架，请手动调整并选择负责人。");

        List<AiTaskItem> items = new ArrayList<>();

        // 阶段1：准备阶段
        AiTaskItem phase1 = new AiTaskItem();
        phase1.setTitle("项目准备阶段");
        phase1.setDescription("明确项目分工与时间节点，准备所需工具与资料，召开项目启动会议。交付物：项目分工表、时间计划表、启动会议纪要。");
        phase1.setPriority("高");
        phase1.setSuggestedDays(2);
        phase1.setAssigneeId(null);  // 兜底方案不指定负责人
        items.add(phase1);

        // 阶段2：资料收集
        AiTaskItem phase2 = new AiTaskItem();
        phase2.setTitle("资料收集与调研");
        phase2.setDescription("收集项目相关的背景资料、数据、文献，进行必要的调研分析。交付物：调研报告、资料汇总文档。");
        phase2.setPriority("高");
        phase2.setSuggestedDays(3);
        phase2.setAssigneeId(null);
        items.add(phase2);

        // 阶段3：执行主体
        AiTaskItem phase3 = new AiTaskItem();
        phase3.setTitle("项目执行主体");
        phase3.setDescription("按照计划推进项目核心工作，完成项目的主要产出。定期同步进度，及时调整偏差。交付物：项目核心成果/产品/文档。");
        phase3.setPriority("高");
        phase3.setSuggestedDays(5);
        phase3.setAssigneeId(null);
        items.add(phase3);

        // 阶段4：汇报总结
        AiTaskItem phase4 = new AiTaskItem();
        phase4.setTitle("项目汇报与总结");
        phase4.setDescription("整理项目成果，制作汇报材料（PPT/文档），进行项目答辩或汇报，撰写项目总结报告。交付物：汇报PPT、项目总结报告。");
        phase4.setPriority("中");
        phase4.setSuggestedDays(3);
        phase4.setAssigneeId(null);
        items.add(phase4);

        response.setItems(items);
        return response;
    }
}

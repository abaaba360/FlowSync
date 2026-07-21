package edu.ustb.flowsync;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.protocol.Protocol;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Scanner;

/**
 * FlowSync AI 任务拆解 —— 独立演示入口（非Spring）
 *
 * <p>参考 mybatis-plus-demo5 中的 Main.java 和
 * {@code AI部分main的java代码示例.txt} 实现。
 * 可通过此类直接测试阿里云千问大模型（qwen-plus）的任务拆解能力，
 * 无需启动 Spring Boot 应用。</p>
 *
 * <h3>使用方式</h3>
 * <pre>
 *   # 命令行运行
 *   mvn compile exec:java -Dexec.mainClass="edu.ustb.flowsync.Main"
 *
 *   # 或在IDE中直接运行本类的 main 方法
 * </pre>
 *
 * @see AiServiceImpl 对应的Spring Service实现
 */
public class Main {

    // ==================== 阿里云百炼配置 ====================
    /** 华北2（北京）地域的工作空间URL，请替换为你的工作空间ID */
    private static final String API_BASE_URL =
            "https://{your-workspace-id}.cn-beijing.maas.aliyuncs.com/api/v1";

    /** API Key（阿里云百炼），请替换为你自己的Key */
    private static final String API_KEY = "sk-your-api-key-here";

    /** 模型列表：https://help.aliyun.com/model-studio/getting-started/models */
    private static final String MODEL_NAME = "qwen-plus";

    // Jackson ObjectMapper
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== System Prompt ====================

    /**
     * 系统提示词——设定AI的身份和通用规则
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

    // ==================== 核心方法：调用千问模型 ====================

    /**
     * 调用阿里云千问大模型进行任务拆解
     *
     * @param projectName      项目名称
     * @param projectGoal      项目目标
     * @param projectDesc      项目补充说明
     * @param membersInfo      可选成员信息（格式："1 - 张三\n2 - 李四"）
     * @return AI原始返回文本（JSON格式）
     * @throws ApiException            API异常
     * @throws NoApiKeyException       API Key缺失异常
     * @throws InputRequiredException  输入缺失异常
     */
    public static GenerationResult callWithMessage(
            String projectName, String projectGoal, String projectDesc, String membersInfo)
            throws ApiException, NoApiKeyException, InputRequiredException {

        // 1、使用工作空间ID生成URL地址
        Generation gen = new Generation(Protocol.HTTP.getValue(), API_BASE_URL);

        // 2、系统提示词：设定AI的身份和通用规则
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(SYSTEM_PROMPT)
                .build();

        // 3、用户提示词：对本次任务的详细描述（对应规格说明书 6.3.5）
        StringBuilder userMessage = new StringBuilder();
        userMessage.append("请将以下项目拆解为多个可执行任务，并以JSON格式输出。\n\n");
        userMessage.append("## 项目信息\n");
        userMessage.append("- 项目名称：").append(projectName).append("\n");
        userMessage.append("- 项目目标：").append(projectGoal).append("\n");
        if (projectDesc != null && !projectDesc.isEmpty()) {
            userMessage.append("- 补充说明：").append(projectDesc).append("\n");
        }
        userMessage.append("\n## 可选成员列表\n");
        userMessage.append(membersInfo).append("\n");
        userMessage.append("\n## 拆解要求\n");
        userMessage.append("1. 任务要去覆盖答辩的全流程（包含但不限于：准备阶段、资料收集、执行主体、汇报总结）\n");
        userMessage.append("2. 每个任务必须包含以下字段，不可遗漏：\n");
        userMessage.append("   - title：任务标题\n");
        userMessage.append("   - description：任务描述（包含：做什么、交付什么，尽量简洁明了）\n");
        userMessage.append("   - priority：优先级（高/中/低）\n");
        userMessage.append("   - suggestedDays：建议完成天数\n");
        userMessage.append("   - assigneeId：任务负责人ID（从可选成员列表中选择，合理分配）\n");
        userMessage.append("3. 责任人分配原则：\n");
        userMessage.append("   - 合理分摊工作量，避免一个人承担过多工作\n");
        userMessage.append("   - 根据任务性质分配责任人（如：汇报可以分配给擅长表达的组员）\n");
        userMessage.append("4. 每项任务都必须有 assigneeId\n");
        userMessage.append("5. 同时请提供一个 summary 字段，用50字以内总结任务拆解思路\n");
        userMessage.append("\n## 严格遵守JSON输出格式\n");
        userMessage.append("{\n");
        userMessage.append("  \"summary\": \"...\",\n");
        userMessage.append("  \"items\": [\n");
        userMessage.append("    {\"title\": \"...\", \"description\": \"...\", \"priority\": \"中\", \"suggestedDays\": 3, \"assigneeId\": 1}\n");
        userMessage.append("  ]\n");
        userMessage.append("}\n");
        userMessage.append("\n只输出一个JSON对象，不能包含说明文字、注释或代码块标记");

        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(userMessage.toString())
                .build();

        // 4、生成查询参数
        GenerationParam param = GenerationParam.builder()
                .apiKey(API_KEY)
                .model(MODEL_NAME)
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();

        return gen.call(param);
    }

    // ==================== JSON格式化输出 ====================

    /**
     * 将AI返回的JSON字符串格式化后打印
     */
    private static void prettyPrintJson(String jsonStr) {
        try {
            // 清理可能的markdown代码块
            String cleaned = jsonStr.trim();
            if (cleaned.startsWith("```json")) cleaned = cleaned.substring(7);
            else if (cleaned.startsWith("```")) cleaned = cleaned.substring(3);
            if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);
            cleaned = cleaned.trim();

            JsonNode root = objectMapper.readTree(cleaned);
            String formatted = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            System.out.println(formatted);
        } catch (Exception e) {
            System.out.println("(原始输出) " + jsonStr);
        }
    }

    // ==================== main 入口 ====================

    /**
     * 程序入口——交互式控制台测试
     *
     * <p>运行后可在控制台输入项目信息，调用千问模型进行任务拆解并打印结果。
     * 按 Ctrl+C 或输入 exit 退出。</p>
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  FlowSync AI 任务拆解 - 控制台测试");
        System.out.println("  模型: " + MODEL_NAME);
        System.out.println("  输入 'exit' 退出程序");
        System.out.println("========================================\n");

        try (Scanner scanner = new Scanner(System.in, "UTF-8")) {
            while (true) {
                System.out.print("请输入项目名称: ");
                String projectName = scanner.nextLine().trim();
                if ("exit".equalsIgnoreCase(projectName)) {
                    System.out.println("程序退出。");
                    break;
                }
                if (projectName.isEmpty()) {
                    System.out.println("项目名称不能为空，请重新输入。\n");
                    continue;
                }

                System.out.print("请输入项目目标: ");
                String projectGoal = scanner.nextLine().trim();

                System.out.print("请输入补充说明（可选，回车跳过）: ");
                String projectDesc = scanner.nextLine().trim();

                System.out.print("请输入成员列表（格式: 1-张三,2-李四...）: ");
                String membersInput = scanner.nextLine().trim();

                // 构建成员信息字符串
                StringBuilder membersInfo = new StringBuilder();
                if (!membersInput.isEmpty()) {
                    String[] memberPairs = membersInput.split(",");
                    for (String pair : memberPairs) {
                        String[] parts = pair.split("-");
                        if (parts.length == 2) {
                            membersInfo.append(parts[0].trim())
                                    .append(" - ")
                                    .append(parts[1].trim())
                                    .append("\n");
                        }
                    }
                }
                if (membersInfo.isEmpty()) {
                    membersInfo.append("1 - 项目负责人\n2 - 成员张三\n3 - 成员李四\n");
                }

                System.out.println("\n正在调用千问模型，请稍候...\n");

                try {
                    GenerationResult result = callWithMessage(
                            projectName, projectGoal, projectDesc, membersInfo.toString());
                    String content = result.getOutput().getChoices().get(0).getMessage().getContent();

                    System.out.println("========== AI 生成结果 ==========");
                    prettyPrintJson(content);
                    System.out.println("==================================\n");

                } catch (ApiException | NoApiKeyException | InputRequiredException e) {
                    System.err.println("AI调用失败: " + e.getMessage());
                    System.err.println("请参考文档: https://help.aliyun.com/model-studio/developer-reference/error-code");

                    // 兜底输出（对应规格说明书 6.3.7）
                    System.out.println("\n========== 兜底方案 ==========");
                    System.out.println("{");
                    System.out.println("  \"summary\": \"AI服务暂时不可用，已为您生成基础四阶段任务框架。\",");
                    System.out.println("  \"items\": [");
                    System.out.println("    {\"title\": \"项目准备阶段\", \"description\": \"明确分工与时间节点，召开启动会议\", \"priority\": \"高\", \"suggestedDays\": 2, \"assigneeId\": 1},");
                    System.out.println("    {\"title\": \"资料收集与调研\", \"description\": \"收集背景资料，进行调研分析\", \"priority\": \"高\", \"suggestedDays\": 3, \"assigneeId\": 2},");
                    System.out.println("    {\"title\": \"项目执行主体\", \"description\": \"推进核心工作，完成主要产出\", \"priority\": \"高\", \"suggestedDays\": 5, \"assigneeId\": 1},");
                    System.out.println("    {\"title\": \"项目汇报与总结\", \"description\": \"整理成果，制作汇报材料，撰写总结报告\", \"priority\": \"中\", \"suggestedDays\": 3, \"assigneeId\": 3}");
                    System.out.println("  ]");
                    System.out.println("}");
                    System.out.println("==================================\n");
                }

                System.out.println("----------------------------------------\n");
            }
        }
    }
}

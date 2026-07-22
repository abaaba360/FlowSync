package edu.ustb.flowsync.controller;

import edu.ustb.flowsync.auth.AuthContext;
import edu.ustb.flowsync.common.ApiResponse;
import edu.ustb.flowsync.dto.AiTaskGenerateRequest;
import edu.ustb.flowsync.dto.AiTaskItem;
import edu.ustb.flowsync.dto.AiTaskResponse;
import edu.ustb.flowsync.dto.MemberInfo;
import edu.ustb.flowsync.entity.TaskInfo;
import edu.ustb.flowsync.entity.User;
import edu.ustb.flowsync.mapper.TaskInfoMapper;
import edu.ustb.flowsync.mapper.UserMapper;
import edu.ustb.flowsync.service.AiService;
import edu.ustb.flowsync.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI智能拆解控制器
 * 对应规格说明书 6.3 AI智能拆解
 * <p>
 * 接口路径对齐前端 flowsync-web/src/api/ai.js：
 * - POST /api/ai/task-plan      → 调用AI生成任务
 * - POST /api/ai/task-plan/import → 确认导入任务到数据库
 * </p>
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AiController {

    @Autowired
    private AiService aiService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TaskInfoMapper taskInfoMapper;

    @Autowired
    private PermissionService permissionService;

    /**
     * AI智能拆解 —— 生成项目任务
     * 对应前端 getAiTaskPlan(params)
     * <p>
     * 后端自动查询全部用户作为"可选成员列表"传给AI，
     * 前端无需传 members 字段。
     * </p>
     */
    @PostMapping("/task-plan")
    public ApiResponse<AiTaskResponse> generateTasks(@RequestBody AiTaskGenerateRequest request) {
        System.out.println("[AiController] task-plan 被调用...");
        System.out.println("[AiController] 项目名称: " + request.getProjectName());
        permissionService.requireProjectOwner(request.getProjectId());

        // 自动查询全部用户，构建成员列表传给AI
        List<User> users = userMapper.selectList(null);
        List<MemberInfo> members = new ArrayList<>();
        if (users != null) {
            for (User u : users) {
                members.add(new MemberInfo(u.getId(), u.getRealName() != null ? u.getRealName() : u.getUsername(), u.getRole()));
            }
        }
        request.setMembers(members);
        System.out.println("[AiController] 成员数量: " + members.size());

        AiTaskResponse result = aiService.generateTasks(request);

        if (result.isFallback()) {
            return ApiResponse.success("AI服务暂不可用，已返回兜底方案，请手动调整", result);
        }
        return ApiResponse.success("AI任务拆解完成，请确认后导入", result);
    }

    /**
     * 导入AI任务 —— 用户在前端确认后，将选中的任务批量入库
     * 对应前端 importAiTasks({projectId, creatorId, items})
     */
    @PostMapping("/task-plan/import")
    public ApiResponse<Object> importTasks(@RequestBody Map<String, Object> body) {
        System.out.println("[AiController] task-plan/import 被调用...");

        Long projectId = body.get("projectId") != null
                ? ((Number) body.get("projectId")).longValue() : null;

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");

        if (projectId == null || items == null || items.isEmpty()) {
            return ApiResponse.fail("项目ID和任务列表不能为空");
        }
        permissionService.requireProjectOwner(projectId);
        Long creatorId = AuthContext.getRequiredCurrentUser().getId();

        int count = 0;
        for (Map<String, Object> item : items) {
            TaskInfo task = new TaskInfo();
            task.setProjectId(projectId);
            task.setCreatorId(creatorId);
            task.setTitle((String) item.get("title"));
            task.setDescription((String) item.get("description"));
            task.setPriority((String) item.getOrDefault("priority", "中"));
            task.setStatus("未开始");
            task.setAssigneeId(item.get("assigneeId") != null
                    ? ((Number) item.get("assigneeId")).longValue() : null);
            if (item.get("suggestedDays") != null) {
                int days = ((Number) item.get("suggestedDays")).intValue();
                task.setDueDate(java.time.LocalDate.now().plusDays(days));
            }
            taskInfoMapper.insert(task);
            count++;
        }

        System.out.println("[AiController] 成功导入 " + count + " 个任务");
        return ApiResponse.success("成功导入 " + count + " 个任务", count);
    }
}

package edu.ustb.flowsync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("task_info")
public class TaskInfo {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;
    private Long parentId;
    private String title;
    private String description;
    private Long assigneeId;
    private Long creatorId;
    private String status;
    private String priority;
    private LocalDate dueDate;
    private String aiSuggestion;
    private LocalDateTime createTime;

    // 以下为展示字段，不存数据库
    @TableField(exist = false)
    private String projectName;
    @TableField(exist = false)
    private String parentTitle;
    @TableField(exist = false)
    private String assigneeName;
    @TableField(exist = false)
    private String creatorName;
}

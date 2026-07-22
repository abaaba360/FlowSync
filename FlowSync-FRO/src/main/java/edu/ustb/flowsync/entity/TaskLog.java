package edu.ustb.flowsync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("task_log")
public class TaskLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;
    private Integer progressPercent;
    private String content;
    private Long operatorId;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String taskTitle;
    @TableField(exist = false)
    private String operatorName;
}

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
@TableName("project_info")
public class ProjectInfo {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String description;
    private String status;
    private String priority;
    private Long ownerId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String ownerName;
}

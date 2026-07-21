package edu.ustb.flowsync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 成员简要信息（用于AI拆解时传递可选成员列表）
 * 对应规格说明书 6.3.5 User Prompt 模板中的"可选成员"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfo {
    /** 成员ID */
    private Long id;
    /** 成员姓名 */
    private String name;
    /** 成员角色 */
    private String role;
}

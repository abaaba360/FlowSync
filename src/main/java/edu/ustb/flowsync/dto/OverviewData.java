package edu.ustb.flowsync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverviewData {
    private Long userCount;
    private Long projectCount;
    private Long taskCount;
    private Long summaryCount;
}

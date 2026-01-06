package kr.ac.koreatech.sw.kosp.domain.github.queue.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QueueStatsResponse {
    
    private Long queueLength;
    private Long processingCount;
    private Long failedCount;
    private Long completedCount;
}

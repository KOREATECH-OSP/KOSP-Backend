package kr.ac.koreatech.sw.kosp.domain.github.queue.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.koreatech.sw.kosp.domain.github.queue.dto.QueueStatsResponse;
import kr.ac.koreatech.sw.kosp.domain.github.queue.model.CollectionJob;
import kr.ac.koreatech.sw.kosp.domain.github.queue.service.CollectionJobMonitoringService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/github/collection")
@RequiredArgsConstructor
public class CollectionMonitoringController {
    
    private final CollectionJobMonitoringService monitoringService;
    
    @GetMapping("/queue/stats")
    public ResponseEntity<QueueStatsResponse> getQueueStats() {
        return ResponseEntity.ok(monitoringService.getQueueStats());
    }
    
    @GetMapping("/queue/failed")
    public ResponseEntity<List<CollectionJob>> getFailedJobs() {
        return ResponseEntity.ok(monitoringService.getFailedJobs());
    }
    
    @PostMapping("/queue/retry/{jobId}")
    public ResponseEntity<Void> retryJob(@PathVariable String jobId) {
        monitoringService.retryJob(jobId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/queue/retry-all-failed")
    public ResponseEntity<Void> retryAllFailed() {
        monitoringService.retryAllFailed();
        return ResponseEntity.ok().build();
    }
}

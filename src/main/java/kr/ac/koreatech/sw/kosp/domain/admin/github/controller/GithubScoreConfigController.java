package kr.ac.koreatech.sw.kosp.domain.admin.github.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubScoreConfig;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubScoreConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/admin/github/score-config")
@RequiredArgsConstructor
@Slf4j
public class GithubScoreConfigController {

    private final GithubScoreConfigRepository scoreConfigRepository;

    @GetMapping
    public ResponseEntity<List<GithubScoreConfig>> getAllConfigs() {
        log.info("Fetching all score configurations");
        List<GithubScoreConfig> configs = scoreConfigRepository.findAll();
        return ResponseEntity.ok(configs);
    }

    @GetMapping("/active")
    public ResponseEntity<GithubScoreConfig> getActiveConfig() {
        log.info("Fetching active score configuration");
        GithubScoreConfig config = scoreConfigRepository.findByActive(true)
            .orElseThrow(() -> new EntityNotFoundException("No active score configuration found"));
        return ResponseEntity.ok(config);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GithubScoreConfig> getConfig(@PathVariable Long id) {
        log.info("Fetching score configuration: {}", id);
        GithubScoreConfig config = scoreConfigRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Score configuration not found: " + id));
        return ResponseEntity.ok(config);
    }

    @PostMapping
    public ResponseEntity<GithubScoreConfig> createConfig(@RequestBody GithubScoreConfig config) {
        log.info("Creating new score configuration: {}", config.getConfigName());

        if (scoreConfigRepository.existsByConfigName(config.getConfigName())) {
            throw new IllegalArgumentException("Configuration name already exists: " + config.getConfigName());
        }

        GithubScoreConfig saved = scoreConfigRepository.save(config);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GithubScoreConfig> updateConfig(
        @PathVariable Long id,
        @RequestBody GithubScoreConfig updateRequest
    ) {
        log.info("Updating score configuration: {}", id);

        GithubScoreConfig config = scoreConfigRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Score configuration not found: " + id));

        config.update(
            updateRequest.getActivityLevelMaxScore(),
            updateRequest.getCommitsWeight(),
            updateRequest.getLinesWeight(),
            updateRequest.getDiversityMaxScore(),
            updateRequest.getDiversityRepoThreshold(),
            updateRequest.getImpactMaxScore(),
            updateRequest.getStarsWeight(),
            updateRequest.getForksWeight(),
            updateRequest.getContributorsWeight(),
            updateRequest.getNightOwlBonus(),
            updateRequest.getEarlyAdopterBonus()
        );

        GithubScoreConfig saved = scoreConfigRepository.save(config);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateConfig(@PathVariable Long id) {
        log.info("Activating score configuration: {}", id);

        // 1. 모든 설정 비활성화
        List<GithubScoreConfig> allConfigs = scoreConfigRepository.findAll();
        allConfigs.forEach(c -> {
            c.deactivate();
            scoreConfigRepository.save(c);
        });

        // 2. 선택한 설정 활성화
        GithubScoreConfig config = scoreConfigRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Score configuration not found: " + id));
        
        config.activate();
        scoreConfigRepository.save(config);

        log.info("Score configuration activated: {} ({})", config.getConfigName(), id);

        return ResponseEntity.ok().build();
    }
}

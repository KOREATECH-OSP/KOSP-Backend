package io.swkoreatech.kosp.domain.challenge.service;

/**
 * 도전 과제 진행 상태 정보.
 *
 * @param current 현재 달성량
 * @param target 목표 달성량
 */
public record ProgressInfo(int current, int target) {

}

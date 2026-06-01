package io.swkoreatech.kosp.domain.title.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.title.model.Title;
import io.swkoreatech.kosp.common.title.model.UserTitle;
import io.swkoreatech.kosp.common.title.repository.TitleConditionRepository;
import io.swkoreatech.kosp.common.title.repository.TitleRepository;
import io.swkoreatech.kosp.common.title.repository.UserTitleRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.title.dto.response.TitleConditionResponse;
import io.swkoreatech.kosp.domain.title.dto.response.TitleDetailResponse;
import io.swkoreatech.kosp.domain.title.dto.response.TitleListResponse;
import io.swkoreatech.kosp.domain.title.dto.response.UserTitleListResponse;
import io.swkoreatech.kosp.domain.title.dto.response.UserTitleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 유저 칭호 조회 및 대표 칭호 설정 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TitleService {

    private final TitleRepository titleRepository;
    private final TitleConditionRepository titleConditionRepository;
    private final UserTitleRepository userTitleRepository;
    private final UserRepository userRepository;

    /**
     * 활성화된 전체 칭호 목록을 달성 조건과 함께 반환한다 (공개).
     *
     * @return 전체 칭호 목록 응답
     */
    public TitleListResponse getAllTitles() {
        List<Title> titles = titleRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc();
        List<TitleDetailResponse> responses = titles.stream()
            .map(title -> {
                List<TitleConditionResponse> conditions = titleConditionRepository
                    .findAllByTitle(title)
                    .stream()
                    .map(TitleConditionResponse::from)
                    .toList();
                return TitleDetailResponse.from(title, conditions);
            })
            .toList();
        return new TitleListResponse(responses, responses.size());
    }

    /**
     * 본인의 보유 칭호 목록을 조회한다.
     *
     * @param user 요청 유저
     * @return 칭호 목록 응답
     */
    public UserTitleListResponse getMyTitles(User user) {
        List<UserTitle> userTitles = userTitleRepository.findAllByUserAndIsRevokedFalse(user);
        return UserTitleListResponse.from(userTitles);
    }

    /**
     * 특정 유저의 보유 칭호 목록을 조회한다 (공개).
     *
     * @param userId 조회 대상 유저 ID
     * @return 칭호 목록 응답
     */
    public UserTitleListResponse getUserTitles(Long userId) {
        User user = userRepository.getById(userId);
        List<UserTitle> userTitles = userTitleRepository.findAllByUserAndIsRevokedFalse(user);
        return UserTitleListResponse.from(userTitles);
    }

    /**
     * 대표 칭호를 설정한다.
     *
     * <p>동일 유저의 기존 대표 칭호를 모두 해제하고 지정된 칭호를 대표로 설정한다.
     * 트랜잭션 처리로 대표 칭호가 항상 최대 1개임을 보장한다.</p>
     *
     * @param userTitleId 대표로 설정할 userTitle ID
     * @param user        요청 유저 (본인 소유 검증)
     * @return 설정된 칭호 응답
     */
    @Transactional
    public UserTitleResponse setDisplayTitle(Long userTitleId, User user) {
        UserTitle target = userTitleRepository.getById(userTitleId);

        // 본인 소유 칭호인지 검증
        if (!target.getUser().getId().equals(user.getId())) {
            throw new GlobalException(ExceptionMessage.TITLE_DISPLAY_FORBIDDEN);
        }
        if (target.isRevoked()) {
            throw new GlobalException(ExceptionMessage.TITLE_REVOKED);
        }

        // 기존 대표 칭호 전체 해제
        List<UserTitle> currentDisplayTitles =
            userTitleRepository.findAllByUserAndIsDisplayTrueAndIsRevokedFalse(user);
        currentDisplayTitles.forEach(ut -> {
            if (!ut.getId().equals(userTitleId)) {
                ut.unsetDisplay();
                userTitleRepository.save(ut);
            }
        });

        // 대표 칭호 설정
        target.setAsDisplay();
        userTitleRepository.save(target);

        log.info("[Title] userId={} 대표 칭호 설정: titleId={} ({})",
            user.getId(), target.getTitle().getId(), target.getTitle().getName());

        return UserTitleResponse.from(target);
    }
}

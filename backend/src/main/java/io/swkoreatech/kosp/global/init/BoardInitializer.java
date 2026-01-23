package io.swkoreatech.kosp.global.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.domain.community.board.model.Board;
import io.swkoreatech.kosp.domain.community.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoardInitializer implements CommandLineRunner {

    private final BoardRepository boardRepository;

    @Override
    public void run(String... args) throws Exception {
        if (boardRepository.count() == 0) {
            log.info("Initializing default boards...");
            saveBoard("자유게시판", "자유롭게 이야기하는 공간", false, false);
            saveBoard("정보공유", "개발 정보를 공유합니다", false, false);
            saveBoard("공지사항", "중요한 공지사항입니다", false, true); // Added common board
            saveBoard("모집공고", "팀원 모집공고 게시판", true, false);
        }
    }

    private void saveBoard(String name, String description, boolean isRecruitAllowed, boolean isNotice) {
        boardRepository.save(Board.builder()
            .name(name)
            .description(description)
            .isRecruitAllowed(isRecruitAllowed)
            .isNotice(isNotice)
            .build());
    }
}

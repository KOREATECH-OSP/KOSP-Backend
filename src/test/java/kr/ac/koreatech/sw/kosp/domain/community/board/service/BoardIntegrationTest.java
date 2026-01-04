package kr.ac.koreatech.sw.kosp.domain.community.board.service;

import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.community.board.repository.BoardRepository;
import kr.ac.koreatech.sw.kosp.global.common.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BoardIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private BoardRepository boardRepository;

    @BeforeEach
    void setup() {
        // Create test boards
        Board board1 = Board.builder()
            .name("자유게시판")
            .description("자유롭게 소통하는 공간")
            .isRecruitAllowed(false)
            .build();
        boardRepository.save(board1);

        Board board2 = Board.builder()
            .name("팀 모집")
            .description("팀원을 모집하는 게시판")
            .isRecruitAllowed(true)
            .build();
        boardRepository.save(board2);

        Board board3 = Board.builder()
            .name("공지사항")
            .description("중요한 공지사항")
            .isRecruitAllowed(false)
            .build();
        boardRepository.save(board3);
    }

    @Test
    @DisplayName("게시판 목록 조회 성공")
    void getBoards_success() throws Exception {
        // when & then
        mockMvc.perform(get("/v1/community/boards"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.boards").isArray())
            .andExpect(jsonPath("$.boards.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)))
            .andExpect(jsonPath("$.boards[0].name").exists())
            .andExpect(jsonPath("$.boards[0].description").exists())
            .andExpect(jsonPath("$.boards[0].isRecruitAllowed").exists());
    }
}

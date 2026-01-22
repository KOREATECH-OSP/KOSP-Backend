package io.swkoreatech.kosp.domain.community.board.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.swkoreatech.kosp.domain.community.board.dto.response.BoardListResponse;
import io.swkoreatech.kosp.domain.community.board.model.Board;
import io.swkoreatech.kosp.domain.community.board.repository.BoardRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardService 단위 테스트")
class BoardServiceTest {

    @InjectMocks
    private BoardService boardService;

    @Mock
    private BoardRepository boardRepository;

    private Board createBoard(Long id, String name) {
        Board board = Board.builder()
            .name(name)
            .description(name + " 게시판")
            .build();
        ReflectionTestUtils.setField(board, "id", id);
        return board;
    }

    @Nested
    @DisplayName("getBoards 메서드")
    class GetBoardsTest {

        @Test
        @DisplayName("게시판 목록이 비어있으면 빈 리스트를 반환한다")
        void returnsEmptyList_whenNoBoards() {
            // given
            given(boardRepository.findAll()).willReturn(Collections.emptyList());

            // when
            BoardListResponse result = boardService.getBoards();

            // then
            assertThat(result.boards()).isEmpty();
        }

        @Test
        @DisplayName("모든 게시판 목록을 반환한다")
        void returnsAllBoards() {
            // given
            Board board1 = createBoard(1L, "자유게시판");
            Board board2 = createBoard(2L, "공지사항");
            given(boardRepository.findAll()).willReturn(List.of(board1, board2));

            // when
            BoardListResponse result = boardService.getBoards();

            // then
            assertThat(result.boards()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getBoard 메서드")
    class GetBoardTest {

        @Test
        @DisplayName("게시판을 ID로 조회한다")
        void returnsBoardById() {
            // given
            Board board = createBoard(1L, "자유게시판");
            given(boardRepository.getById(1L)).willReturn(board);

            // when
            Board result = boardService.getBoard(1L);

            // then
            assertThat(result.getName()).isEqualTo("자유게시판");
        }
    }
}

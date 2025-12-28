package kr.ac.koreatech.sw.kosp.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ExceptionMessage {
    DUPLICATION_NICKNAME("중복된 닉네임입니다.", HttpStatus.CONFLICT),
    AUTHENTICATION("잘못된 인증입니다.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("인증 정보가 없습니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("권한이 없습니다.", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND("유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    BOARD_NOT_FOUND("게시판을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ARTICLE_NOT_FOUND("게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    RECRUITMENT_NOT_FOUND("모집 공고를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    BAD_REQUEST("잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    NEED_EMAIL("닉네임 또는 이메일 중 하나는 필수입니다.", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN("유효하지 않은 토큰이거나 토큰이 없습니다.", HttpStatus.UNAUTHORIZED),
    USER_ALREADY_EXISTS("이미 존재하는 유저입니다.", HttpStatus.CONFLICT),
    CONFLICT("이미 존재하는 리소스입니다.", HttpStatus.CONFLICT),
    NOT_FOUND("리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CHALLENGE_NOT_FOUND("챌린지를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_CHALLENGE_CONDITION("유효하지 않은 챌린지 조건식입니다.", HttpStatus.BAD_REQUEST),
    ALREADY_REPORTED("이미 신고한 게시글입니다.", HttpStatus.CONFLICT),
    SELF_REPORT_NOT_ALLOWED("본인의 게시글은 신고할 수 없습니다.", HttpStatus.BAD_REQUEST),
    ;

    private final String message;
    private final HttpStatus status;

    ExceptionMessage(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}

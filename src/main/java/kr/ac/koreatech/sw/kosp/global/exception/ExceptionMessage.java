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
    SERVER_ERROR("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ROLE_NOT_FOUND("권한을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    GITHUB_USER_NOT_FOUND("연동된 깃허브 계정이 없습니다.", HttpStatus.NOT_FOUND),
    GITHUB_USER_ALREADY_EXISTS("이미 가입된 깃허브 계정입니다.", HttpStatus.CONFLICT),
    GITHUB_CLIENT_REGISTRATION_ERROR("깃허브 클라이언트 설정을 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_SEND_FAILED("이메일 전송에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_NOT_FOUND("이메일 인증 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_VERIFICATION_CODE("인증 코드가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_VERIFIED("이메일 인증이 완료되지 않았습니다.", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_ADDRESS("유효하지 않는 이메일 주소입니다.", HttpStatus.BAD_REQUEST),
    TEAM_NOT_FOUND("소속된 팀을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    TEAM_ALREADY_JOINED("이미 팀에 소속된 사용자입니다.", HttpStatus.CONFLICT),
    INVITATION_EXPIRED("만료된 초대입니다.", HttpStatus.BAD_REQUEST),
    LEADER_CANNOT_LEAVE("팀장은 탈퇴하거나 제명될 수 없습니다.", HttpStatus.BAD_REQUEST),
    APPLICATION_NOT_FOUND("지원 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ALREADY_DECIDED("이미 처리된 지원입니다.", HttpStatus.CONFLICT),
    RECRUIT_CLOSED("마감된 모집 공고입니다.", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_POINTS("포인트가 부족합니다.", HttpStatus.BAD_REQUEST),
    INVALID_POINT_AMOUNT("포인트 금액이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    POINT_TRANSACTION_NOT_FOUND("포인트 거래 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    BANNER_NOT_FOUND("배너를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    BANNER_ALREADY_EXISTS("이미 배너로 등록된 게시글입니다.", HttpStatus.CONFLICT),

    ;

    private final String message;
    private final HttpStatus status;

    ExceptionMessage(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}

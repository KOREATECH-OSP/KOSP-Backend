package io.swkoreatech.kosp.domain.auth.dto.response;

/**
 * 학번/사번 중복 확인 응답 DTO.
 *
 * @param success   요청 성공 여부
 * @param available 사용 가능 여부
 * @param message   결과 메시지
 */
public record CheckMemberIdResponse(
    boolean success,
    boolean available,
    String message
) {
    /**
     * 사용 가능 여부와 메시지로부터 응답 DTO를 생성한다.
     *
     * @param available 사용 가능 여부
     * @param message   결과 메시지
     * @return 학번/사번 중복 확인 응답 DTO
     */
    public static CheckMemberIdResponse from(boolean available, String message) {
        return new CheckMemberIdResponse(true, available, message);
    }
}

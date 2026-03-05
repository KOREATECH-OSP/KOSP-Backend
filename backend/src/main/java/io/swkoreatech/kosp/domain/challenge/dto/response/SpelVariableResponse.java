package io.swkoreatech.kosp.domain.challenge.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * SpEL 변수 정보 응답 DTO.
 * 도전 과제 조건식에서 사용 가능한 변수 목록과 예제 표현식을 제공한다.
 *
 * @param variables 사용 가능한 변수 목록
 * @param examples 예제 표현식 목록
 */
@Schema(description = "SpEL 변수 정보 응답")
public record SpelVariableResponse(
    @Schema(description = "사용 가능한 변수 목록")
    List<VariableInfo> variables,

    @Schema(description = "예제 표현식")
    List<ExampleExpression> examples
) {
    /**
     * 변수 정보와 예제 표현식으로부터 응답 객체를 생성한다.
     *
     * @param variables 변수 정보 목록
     * @param examples 예제 표현식 목록
     * @return SpEL 변수 정보 응답
     */
    public static SpelVariableResponse from(List<VariableInfo> variables, List<ExampleExpression> examples) {
        return new SpelVariableResponse(variables, examples);
    }
    /**
     * 변수 정보.
     *
     * @param path 변수 경로
     * @param description 변수 설명
     * @param type 데이터 타입
     */
    @Schema(description = "변수 정보")
    public record VariableInfo(
        @Schema(description = "변수 경로", example = "#activity['commits']")
        String path,
        
        @Schema(description = "변수 설명", example = "총 커밋 수")
        String description,
        
        @Schema(description = "데이터 타입", example = "Integer")
        String type
    ) {}

    /**
     * 예제 표현식.
     *
     * @param condition 조건 표현식
     * @param description 설명
     */
    @Schema(description = "예제 표현식")
    public record ExampleExpression(
        @Schema(description = "조건 표현식", example = "#activity['commits'] >= 100")
        String condition,
        
        @Schema(description = "설명", example = "커밋 100회 이상")
        String description
    ) {}
}

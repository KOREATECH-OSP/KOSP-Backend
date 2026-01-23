package io.swkoreatech.kosp.domain.challenge.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "SpEL 변수 정보 응답")
public record SpelVariableResponse(
    @Schema(description = "사용 가능한 변수 목록")
    List<VariableInfo> variables,
    
    @Schema(description = "예제 표현식")
    List<ExampleExpression> examples
) {
    @Schema(description = "변수 정보")
    public record VariableInfo(
        @Schema(description = "변수 경로", example = "#activity['commits']")
        String path,
        
        @Schema(description = "변수 설명", example = "총 커밋 수")
        String description,
        
        @Schema(description = "데이터 타입", example = "Integer")
        String type
    ) {}

    @Schema(description = "예제 표현식")
    public record ExampleExpression(
        @Schema(description = "조건 표현식", example = "#activity['commits'] >= 100")
        String condition,
        
        @Schema(description = "설명", example = "커밋 100회 이상")
        String description
    ) {}
}

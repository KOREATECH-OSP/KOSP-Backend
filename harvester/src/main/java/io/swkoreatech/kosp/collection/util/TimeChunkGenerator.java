package io.swkoreatech.kosp.collection.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 시간 범위를 월 단위 청크로 분할하는 유틸리티 클래스.
 *
 * <p>GitHub 기여 데이터를 월별로 나누어 조회할 때 사용된다.
 * GraphQL API의 기여 조회 기간 제한을 우회하기 위해 긴 기간을 월 단위로 분할한다.
 */
public class TimeChunkGenerator {

    /**
     * 시작 시간부터 종료 시간까지 월 단위 청크 목록을 생성한다.
     *
     * @param start 시작 시간
     * @param end   종료 시간
     * @return 월 단위로 분할된 {@link TimeChunk} 목록
     */
    public static List<TimeChunk> generateMonthlyChunks(ZonedDateTime start, ZonedDateTime end) {
        List<TimeChunk> chunks = new ArrayList<>();
        ZonedDateTime cursor = start;
        
        while (cursor.isBefore(end)) {
            ZonedDateTime chunkEnd = cursor.plusMonths(1);
            
            if (chunkEnd.isAfter(end)) {
                chunkEnd = end;
            }
            
            chunks.add(new TimeChunk(cursor, chunkEnd.plusSeconds(1)));
            
            cursor = chunkEnd;
            
            // Explicit termination to prevent infinite loop
            if (cursor.equals(end)) {
                break;
            }
        }
        
        return chunks;
    }
    
    /**
     * 시간 청크를 나타내는 레코드.
     *
     * @param start 청크 시작 시간
     * @param end   청크 종료 시간
     */
    public static record TimeChunk(
        ZonedDateTime start,
        ZonedDateTime end
    ) {
        /**
         * 시작 시간을 ISO 인스턴트 형식 문자열로 반환한다.
         *
         * @return ISO 인스턴트 형식의 시작 시간 문자열
         */
        public String getStartFormatted() {
            return start.format(DateTimeFormatter.ISO_INSTANT);
        }
        
        /**
         * 종료 시간을 ISO 인스턴트 형식 문자열로 반환한다.
         *
         * @return ISO 인스턴트 형식의 종료 시간 문자열
         */
        public String getEndFormatted() {
            return end.format(DateTimeFormatter.ISO_INSTANT);
        }
    }
}

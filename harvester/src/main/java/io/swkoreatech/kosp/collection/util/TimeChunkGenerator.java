package io.swkoreatech.kosp.collection.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TimeChunkGenerator {
    
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
    
    public static record TimeChunk(
        ZonedDateTime start,
        ZonedDateTime end
    ) {
        public String getStartFormatted() {
            return start.format(DateTimeFormatter.ISO_INSTANT);
        }
        
        public String getEndFormatted() {
            return end.format(DateTimeFormatter.ISO_INSTANT);
        }
    }
}

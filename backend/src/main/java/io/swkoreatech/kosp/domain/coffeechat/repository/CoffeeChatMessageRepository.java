package io.swkoreatech.kosp.domain.coffeechat.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import io.swkoreatech.kosp.domain.coffeechat.document.CoffeeChatMessage;

public interface CoffeeChatMessageRepository extends MongoRepository<CoffeeChatMessage, String> {

    List<CoffeeChatMessage> findByRoomIdOrderBySentAtAsc(Long roomId);

    long countByRoomIdAndSenderIdNotAndIsReadFalse(Long roomId, Long senderId);

    @Query("{ 'roomId': { $in: ?0 }, 'senderId': { $ne: ?1 }, 'isRead': false }")
    List<CoffeeChatMessage> findUnreadByRoomIdsAndNotSender(List<Long> roomIds, Long senderId);

    List<CoffeeChatMessage> findByRoomIdAndSenderIdNotAndIsReadFalse(Long roomId, Long senderId);
}

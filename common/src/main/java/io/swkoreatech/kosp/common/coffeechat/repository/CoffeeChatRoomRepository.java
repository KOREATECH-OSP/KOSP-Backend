package io.swkoreatech.kosp.common.coffeechat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.swkoreatech.kosp.common.coffeechat.model.CoffeeChatRoom;
import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;

public interface CoffeeChatRoomRepository extends JpaRepository<CoffeeChatRoom, Long> {

    @Query("SELECT r FROM CoffeeChatRoom r WHERE (r.user1.id = :u1 AND r.user2.id = :u2) OR (r.user1.id = :u2 AND r.user2.id = :u1)")
    Optional<CoffeeChatRoom> findByParticipants(@Param("u1") Long userId1, @Param("u2") Long userId2);

    @Query("SELECT r FROM CoffeeChatRoom r WHERE r.user1.id = :userId OR r.user2.id = :userId ORDER BY CASE WHEN r.lastMessageAt IS NULL THEN 1 ELSE 0 END, r.lastMessageAt DESC")
    List<CoffeeChatRoom> findAllByUserId(@Param("userId") Long userId);

    default CoffeeChatRoom getById(Long id) {
        return findById(id).orElseThrow(() -> new GlobalException(ExceptionMessage.COFFEE_CHAT_ROOM_NOT_FOUND));
    }
}

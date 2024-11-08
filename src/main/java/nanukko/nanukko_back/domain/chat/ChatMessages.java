package nanukko.nanukko_back.domain.chat;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import nanukko.nanukko_back.domain.user.User;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
public class ChatMessages {
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long chatMessageId; //채팅 메시지 ID

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    private User sender; //발신자 ID (FK)

    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    @NotNull
    private ChatRoom chatRoom; //채팅방 ID (FK)

    @NotNull
    @Column(name = "chat_message")
    private String chatMessage; //채팅 내용

    @NotNull
    @Column(name = "created_at")
    private LocalDateTime createdAt; //생성 날짜

    @NotNull
    @Column(name = "is_read")
    private boolean isRead; //읽음 여부 -> true = 읽음, false = 안읽음

    private String image; //이미지

    @NotNull
    @Column(name = "is_latest")
    private boolean isLatest; //최신메시지
}

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long chatMessageId; //채팅 메시지 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @NotNull
    private User sender; //발신자 ID (FK)

    @ManyToOne(fetch = FetchType.LAZY)
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
    private boolean isRead=false; //읽음 여부 -> true = 읽음, false = 안읽음(기본)

    private String image; //이미지

    @NotNull
    @Column(name = "is_latest")
    private boolean isLatest; //최신메시지

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType type = MessageType.CHAT; // 기본값은 일반 채팅

    //메시지 읽음 처리
    public void UnreadToRead(String userId){
        if(!this.getSender().getUserId().equals(userId)){//상대방이 나한테 보낸 메시지라면?
            this.isRead = true;
        }
    }

    //최신 메시지를 다시 false로 되돌리기
    public void changeNotLatest(){
        if(!this.isLatest){//true라면
            this.isLatest = false;
        }
    }
}

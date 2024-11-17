package nanukko.nanukko_back.domain.notification;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import nanukko.nanukko_back.domain.user.User;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId; //알림 PK

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    private User receiver; //알림 받는 사용자 아이디

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)  // 길이 명시
    @NotNull
    private NotificationType type; //알림 타입

    private String url; //알림을 클릭했을 때 이동할 페이지 링크를 저장

    @NotNull
    @Column(name = "is_read")
    public boolean isRead; //읽음 여부 -> true = 읽음, false = 안읽음

    @NotNull
    private String content; //알림 내용

    @NotNull
    @Column(name = "created_at")
    private LocalDateTime createdAt; //알림 생성 날짜

    @Column(name = "read_at")
    private LocalDateTime readAt; // 읽은 시간

    @Builder
    public Notification(User receiver, NotificationType type, String content, String url, Boolean isRead, LocalDateTime createdAt) {
        this.receiver = receiver;
        this.type = type;
        this.content = content;
        this.url = url;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

//    관리자가 알림 수정 가능한 부분이 있다면 사용할 부분
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt; //알림 수정 날짜
}

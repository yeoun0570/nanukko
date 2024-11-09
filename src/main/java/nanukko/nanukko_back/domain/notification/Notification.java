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
    @NotNull
    @Column(name = "notice_id")
    private Long noticeId; //알림 PK

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    private User user; //알림 받는 사용자 아이디

    @Enumerated(EnumType.STRING)
    @NotNull
    private NotificationType type; //알림 타입

    @NotNull
    @Column(name = "created_at")
    private LocalDateTime createdAt; //알림 생성 날짜
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; //알림 수정 날짜

    @NotNull
    @Column(name = "is_read")
    private boolean isRead; //읽음 여부 -> true = 읽음, false = 안읽음
}

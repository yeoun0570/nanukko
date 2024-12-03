package nanukko.nanukko_back.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadMessageRequest {
    private List<Integer> messageIds; // 여러 메시지 ID를 받을 수 있도록 수정
    private String userId;
    private int page;
    private int size;
}
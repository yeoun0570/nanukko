package nanukko.nanukko_back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.config.FrontendURL;
import nanukko.nanukko_back.config.MailConfig;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.repository.ProductRepository;
import nanukko.nanukko_back.repository.UserRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class MailService {
    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final MailConfig mailConfig;
    private final FrontendURL frontendURL;

/*
1. setTo - 상대방 이메일 주소

2. setSubject - 제목

3. setFrom - 프로퍼티즈에서 공통으로 설정 해둔 출발 이메일

4. setText - 내용 */

    //사용자가 자신이 판매하는 상품이 결제가 되었을 때 배송해달라는 이메일
    @Async // 이메일 전송은 비동기로 처리
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 충분
    public void sendMailConfirmPaymentToSeller(String userId, Long productId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자의 이메일을 찾을 수 없습니다."));

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

            SimpleMailMessage message = getSimpleMailMessage(user.getEmail(), user, product);

            javaMailSender.send(message);
            log.info("메일 전송 성공 - userId: {}, email: {}, productId: {}", userId , userId, productId);

        } catch (Exception e) {
            log.error("메일 전송 실패 - email: {}, productId: {}", userId, e.getMessage());
            throw new RuntimeException("메일 전송 실패", e);
        }
    }

    private SimpleMailMessage getSimpleMailMessage(String email, User user, Product product) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject(String.format("%s님! 올려두신 %s 상품이 결제되었어요!",
                user.getNickname(), product.getProductName()));
        message.setFrom(mailConfig.getServerEmail());

        // 프론트엔드 URL은 설정으로 관리하는 것이 좋음
        message.setText(String.format("%s님이 올려두신 상품이 판매되었어요! " +
                        "확인하고 배송을 보내주세요! %s/my-store/sale-products",
                user.getNickname(), frontendURL.getUrl()));
        return message;
    }
}

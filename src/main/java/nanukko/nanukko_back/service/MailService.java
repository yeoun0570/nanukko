package nanukko.nanukko_back.service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.config.FrontendURL;
import nanukko.nanukko_back.config.MailConfig;
import nanukko.nanukko_back.domain.order.Orders;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.repository.OrderRepository;
import nanukko.nanukko_back.repository.ProductRepository;
import nanukko.nanukko_back.repository.UserRepository;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
    private final OrderRepository orderRepository;
    private final MailConfig mailConfig;
    private final FrontendURL frontendURL;

/*
1. setTo - 상대방 이메일 주소

2. setSubject - 제목

3. setFrom - 프로퍼티즈에서 공통으로 설정 해둔 출발 이메일

4. setText - 내용
*/

    ///////////////////////////////판매자에게 보내는 이메일

    //사용자가 자신이 판매하는 상품이 결제가 되었을 때 배송해달라는 이메일
    @Async // 이메일 전송은 비동기로 처리
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 충분
    public void sendMailConfirmPaymentToSeller(String userId, Long productId) {
        try {
            User user = findAndValidateUser(userId);
            Product product = findAndValidateProduct(productId);

            MimeMessage message = getMimeMessageToSeller(
                    MailTemplate.PAYMENT_CONFIRM, user.getEmail(), user, product);

            javaMailSender.send(message);
            log.info("메일 전송 성공 - userId: {}, productId: {}", userId, productId);

        } catch (Exception e) {
            log.error("메일 전송 실패 - userId: {}, error: {}", userId, e.getMessage());
            throw new RuntimeException("메일 전송 실패", e);
        }
    }

    //결제 취소되었다는 이메일
    @Async
    @Transactional(readOnly = true)
    public void sendMailCancelPaymentToSeller(String userId, Long productId) {
        try {
            User user = findAndValidateUser(userId);
            Product product = findAndValidateProduct(productId);

            MimeMessage message = getMimeMessageToSeller(
                    MailTemplate.PAYMENT_CANCEL, user.getEmail(), user, product);

            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("메일 전송 실패 - userId: {}, error: {}", userId, e.getMessage());
            throw new RuntimeException("메일 전송 실패", e);
        }
    }

    // 구매 확정되었다는 이메일
    @Async
    @Transactional(readOnly = true)
    public void sendMailConfirmPurchaseToSeller(String userId, Long productId) {
        try {
            User user = findAndValidateUser(userId);
            Product product = findAndValidateProduct(productId);

            MimeMessage message = getMimeMessageToSeller(
                    MailTemplate.PURCHASE_CONFIRM, user.getEmail(), user, product);

            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("메일 전송 실패 - userId: {}, error: {}", userId, e.getMessage());
            throw new RuntimeException("메일 전송 실패", e);
        }
    }

    private MimeMessage getMimeMessageToSeller(MailTemplate template, String email, User user, Product product) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            // 발신자 이름 설정 ("나누꼬" <이메일주소>)
            helper.setFrom(new InternetAddress(mailConfig.getServerEmail(), "나누꼬"));
            helper.setTo(email);

            switch (template) {
                case PAYMENT_CONFIRM -> {
                    helper.setSubject(String.format("%s님! 올려두신 '%s' 상품이 결제되었어요!",
                            user.getNickname(), product.getProductName()));
                    helper.setText(String.format("%s님이 올려두신 상품이 판매되었어요! " +
                                    "확인하고 배송을 보내주세요!\n%s/my-store/sale-products",
                            user.getNickname(), frontendURL.getUrl()));
                }
                case PAYMENT_CANCEL -> {
                    helper.setSubject(String.format("%s님, '%s' 상품의 결제가 취소되었어요.",
                            user.getNickname(), product.getProductName()));
                    helper.setText(String.format("'%s' 상품의 결제가 취소되었어요. " +
                                    "상품을 확인해주세요.\n%s/my-store/sale-products",
                            product.getProductName(), frontendURL.getUrl()));
                }
                case PURCHASE_CONFIRM -> {
                    helper.setSubject(String.format("%s님! '%s' 상품이 구매 확정되었어요!",
                            user.getNickname(), product.getProductName()));
                    helper.setText(String.format("%s님의 '%s' 상품이 구매 확정되었어요! " +
                                    "상품을 확인하러 가볼까요?.\n%s/my-store/sale-products",
                            user.getNickname(), product.getProductName(), frontendURL.getUrl()));
                }
            }
            return mimeMessage;
        } catch (Exception e) {
            throw new RuntimeException("메일 메시지 생성 실패", e);
        }
    }

    //////////////////////////////////////////구매자에게 보내는 이메일

    // 배송 출발을 구매자한테 보내는 이메일
    @Async // 이메일 전송은 비동기로 처리
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 충분
    public void sendMailStartDeliveryToBuyer(String userId, String orderId) {
        try {
            User user = findAndValidateUser(userId);
            Orders order = findAndValidateOrder(orderId);

            MimeMessage message = getMimeMessageToBuyer(
                    MailTemplate.START_DELIVERY, user.getEmail(), user, order);

            javaMailSender.send(message);
            log.info("메일 전송 성공 - userId: {}, orderId: {}", userId, orderId);

        } catch (Exception e) {
            log.error("메일 전송 실패 - userId: {}, error: {}", userId, e.getMessage());
            throw new RuntimeException("메일 전송 실패", e);
        }
    }

    // 배송 완료를 구매자한테 보내는 이메일
    @Async // 이메일 전송은 비동기로 처리
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 충분
    public void sendMailDeliveredToBuyer(String userId, String orderId) {
        try {
            User user = findAndValidateUser(userId);
            Orders order = findAndValidateOrder(orderId);

            MimeMessage message = getMimeMessageToBuyer(
                    MailTemplate.DELIVERED, user.getEmail(), user, order);

            javaMailSender.send(message);
            log.info("메일 전송 성공 - userId: {}, orderId: {}", userId, orderId);

        } catch (Exception e) {
            log.error("메일 전송 실패 - userId: {}, error: {}", userId, e.getMessage());
            throw new RuntimeException("메일 전송 실패", e);
        }
    }

    private MimeMessage getMimeMessageToBuyer(MailTemplate template, String email, User user, Orders order) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            // 발신자 이름 설정 ("나누꼬" <이메일주소>)
            helper.setFrom(new InternetAddress(mailConfig.getServerEmail(), "나누꼬"));
            helper.setTo(email);

            switch (template) {
                case START_DELIVERY -> {
                    helper.setSubject(String.format("%s님! '%s' 상품의 배송이 출발했어요!",
                            user.getNickname(), order.getProduct().getProductName()));
                    helper.setText(String.format("%s님이 구매한 상품의 배송이 출발했어요! " +
                                    "확인하러 가볼까요?\n%s/my-store/buy-products",
                            user.getNickname(), frontendURL.getUrl()));
                }
                case DELIVERED -> {
                    helper.setSubject(String.format("%s님, %s 상품의 배송이 도착했어요!",
                            user.getNickname(), order.getProduct().getProductName()));
                    helper.setText(String.format("%s님! 구매하신 '%s' 상품의 배송이 도착했어요. " +
                                    "확인하러 가볼까요?\n%s/my-store/sale-products",
                            user.getNickname(), order.getProduct().getProductName(), frontendURL.getUrl()));
                }
            }
            return mimeMessage;
        } catch (Exception e) {
            throw new RuntimeException("메일 메시지 생성 실패", e);
        }
    }


    ////////////////////////////////////공용으로 쓰기위한 곳


    @Transactional(readOnly = true)
    protected User findAndValidateUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    //상품 검색
    @Transactional(readOnly = true)
    protected Product findAndValidateProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    protected Orders findAndValidateOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
    }

}

package nanukko.nanukko_back.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.chat.ChatMessages;
import nanukko.nanukko_back.domain.chat.ChatRoom;
import nanukko.nanukko_back.domain.chat.MessageType;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.dto.chat.ChatMessageDTO;
import nanukko.nanukko_back.dto.chat.ChatRoomDTO;
import nanukko.nanukko_back.dto.page.PageResponseDTO;
import nanukko.nanukko_back.repository.ProductRepository;
import nanukko.nanukko_back.repository.UserRepository;
import nanukko.nanukko_back.repository.chat.ChatMessageRepository;
import nanukko.nanukko_back.repository.chat.ChatRoomRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional  // 클래스 레벨에 트랜잭션 적용
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    private final ChatMessageRepository chatMessageRepository;


    /*채팅방 목록 조회*/
    public PageResponseDTO<ChatRoomDTO> getChatRooms(String userId, Pageable pageable) {
        // 1단계: DB에서 페이징된 데이터 조회
        Page<ChatRoom> chatRoomPage = chatRoomRepository.
                findActiveRoomsByUserId
                        (userId, pageable);

        // 2단계: Entity를 DTO로 직접 변환
        Page<ChatRoomDTO> dtoPage = chatRoomPage.map(ChatRoomDTO::from);

        // 3단계: Page<ChatRoomDTO>를 PageResponseDTO로 포장
        return new PageResponseDTO<>(dtoPage);
    }//end of getChatRooms


    /*새 채팅방 생성*/
    public ChatRoomDTO createChatRoom(String userId, Long productId){//구매자 아이디, 상품 정보 파라미터로 받음

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        /*존재하는 채팅방인지 검사*/
        Optional<ChatRoom> existingChatRoom = chatRoomRepository
                .findByBuyer_UserIdAndProduct_ProductId(userId, productId);
        if(existingChatRoom.isPresent()){//이미 존재하는 채팅방이면 기존 채팅방 반환
            ChatRoom chatRoom = existingChatRoom.get();
            return ChatRoomDTO.from(chatRoom);
        }

        // 로그인한 사용자(buyer) 조회
        User buyer = userRepository.findById(userId).get(); // 이미 로그인 해서 인증된 사용자이므로 Optional이나 예외처리 불필요

        ChatRoom newChatRoom = ChatRoom.createChatRoom(product,buyer);
        ChatRoom savedChatRoom = chatRoomRepository.save(newChatRoom);//영속성 컨텍스트에 저장(save() 호출로 실제 DB에 저장 + 저장할 때 id값 생성됨)

        return ChatRoomDTO.from(savedChatRoom);
    }//createChatRoom

    /**
     * 채팅방 생성 또는 재입장
     * - 같은 상품에 대한 채팅방이 있으면 재사용
     * - 없으면 새로 생성
     */
    public ChatRoomDTO createOrReturnToChatRoom(String userId, Long productId, Pageable pageable) {
        // 1. 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        // 2. 기존 채팅방 찾기
        Optional<ChatRoom> existingChatRoom;
        if (product.getSeller().getUserId().equals(userId)) {
            // 판매자인 경우
            existingChatRoom = chatRoomRepository
                    .findByProduct_ProductIdAndProduct_Seller_UserId(productId, userId);
        } else {
            // 구매자인 경우
            existingChatRoom = chatRoomRepository
                    .findByBuyer_UserIdAndProduct_ProductId(userId, productId);
        }

        // 3. 기존 채팅방이 있는 경우
        if (existingChatRoom.isPresent()) {
            ChatRoom chatRoom = existingChatRoom.get();

            // 해당 채팅방의 메시지만 조회
            Page<ChatMessages> messages = chatMessageRepository
                    .findMessagesSinceLastExit(chatRoom.getChatRoomId(),userId, pageable);
                    //.findByChatRoom_ChatRoomIdOrderByCreatedAtDesc(chatRoom.getChatRoomId(), pageable);

            List<ChatMessageDTO> messageDTOs = messages.getContent().stream()
                    .map(ChatMessageDTO::from)
                    .collect(Collectors.toList());

            return ChatRoomDTO.builder()
                    .chatRoomId(chatRoom.getChatRoomId())
                    .productId(chatRoom.getProduct().getProductId())
                    .productName(chatRoom.getProduct().getProductName())
                    .buyerId(chatRoom.getBuyer().getUserId())
                    .buyerName(chatRoom.getBuyer().getNickname())
                    .sellerId(chatRoom.getProduct().getSeller().getUserId())
                    .sellerName(chatRoom.getProduct().getSeller().getNickname())
                    .chatMessages(messageDTOs)
                    .createdAt(chatRoom.getCreatedAt())
                    .updatedAt(chatRoom.getUpdatedAt())
                    .sellerLeftAt(chatRoom.getSellerLeftAt())
                    .buyerLeftAt(chatRoom.getBuyerLeftAt())
                    .build();
        }

        // 4. 새 채팅방 생성 (구매자만 가능)
        if (product.getSeller().getUserId().equals(userId)) {
            throw new IllegalStateException("판매자는 새로운 채팅방을 생성할 수 없습니다.");
        }

        User buyer = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        ChatRoom newChatRoom = ChatRoom.createChatRoom(product, buyer);
        ChatRoom savedChatRoom = chatRoomRepository.save(newChatRoom);

        return ChatRoomDTO.builder()
                .chatRoomId(savedChatRoom.getChatRoomId())
                .productId(savedChatRoom.getProduct().getProductId())
                .productName(savedChatRoom.getProduct().getProductName())
                .buyerId(savedChatRoom.getBuyer().getUserId())
                .buyerName(savedChatRoom.getBuyer().getNickname())
                .sellerId(savedChatRoom.getProduct().getSeller().getUserId())
                .sellerName(savedChatRoom.getProduct().getSeller().getNickname())
                .chatMessages(new ArrayList<>())
                .createdAt(savedChatRoom.getCreatedAt())
                .updatedAt(savedChatRoom.getUpdatedAt())
                .sellerLeftAt(savedChatRoom.getSellerLeftAt())
                .buyerLeftAt(savedChatRoom.getBuyerLeftAt())
                .build();
    }
//    public ChatRoomDTO createOrReturnToChatRoom(String userId, Long productId, Pageable pageable) {
//        // 1. 상품 조회
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));
//
//        // 2. 기존 채팅방 찾기 (판매자/구매자 구분)
//        Optional<ChatRoom> existingChatRoom;
//
//        if (product.getSeller().getUserId().equals(userId)) {
//            // 판매자인 경우
//            existingChatRoom = chatRoomRepository
//                    .findByProduct_ProductIdAndProduct_Seller_UserIdAndSellerLeftAtIsNotNull(productId, userId);
//        } else {
//            // 구매자인 경우
//            existingChatRoom = chatRoomRepository
//                    .findByBuyer_UserIdAndProduct_ProductId(userId, productId);
//        }
//
//        // 3. 기존 채팅방이 있는 경우
//        if (existingChatRoom.isPresent()) {
//            ChatRoom chatRoom = existingChatRoom.get();
//
//            // 메시지 필터링 (재입장 시점 이후 메시지만)
//            List<ChatMessageDTO> messageDTOList = chatMessageRepository
//                    .findMessagesSinceLastExit(chatRoom.getChatRoomId(), userId, pageable)
//                    .getContent()
//                    .stream()
//                    .map(ChatMessageDTO::from)
//                    .collect(Collectors.toList());
//
//
//
//
//            // builder를 사용한 DTO 생성
//            return ChatRoomDTO.builder()
//                    .chatRoomId(chatRoom.getChatRoomId())
//                    // 상품 정보
//                    .productId(chatRoom.getProduct().getProductId())
//                    .productName(chatRoom.getProduct().getProductName())
//                    // 구매자 정보
//                    .buyerId(chatRoom.getBuyer().getUserId())
//                    .buyerName(chatRoom.getBuyer().getNickname())
//                    // 판매자 정보
//                    .sellerId(chatRoom.getProduct().getSeller().getUserId())
//                    .sellerName(chatRoom.getProduct().getSeller().getNickname())
//                    .chatMessages(messageDTOList)
//                    // 시간 정보
//                    .createdAt(chatRoom.getCreatedAt())
//                    .updatedAt(chatRoom.getUpdatedAt())
//                    .sellerLeftAt(chatRoom.getSellerLeftAt())
//                    .buyerLeftAt(chatRoom.getBuyerLeftAt())
//                    .build();
//        }
//
//        // 4. 새 채팅방 생성 (구매자만 가능)
//        if (product.getSeller().getUserId().equals(userId)) {
//            throw new IllegalStateException("판매자는 자기 상품에 대해 채팅을 할 수 없습니다.");
//        }
//
//        User buyer = userRepository.findById(userId)
//                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
//
//        // 새 채팅방 생성 및 저장
//        ChatRoom newChatRoom = ChatRoom.createChatRoom(product, buyer);
//        ChatRoom savedChatRoom = chatRoomRepository.save(newChatRoom);
//
//        // 새로 생성된 채팅방은 메시지가 없으므로 빈 리스트로 생성
//        return ChatRoomDTO.builder()
//                .chatRoomId(savedChatRoom.getChatRoomId())
//                // 상품 정보
//                .productId(savedChatRoom.getProduct().getProductId())
//                .productName(savedChatRoom.getProduct().getProductName())
//                // 구매자 정보
//                .buyerId(savedChatRoom.getBuyer().getUserId())
//                .buyerName(savedChatRoom.getBuyer().getNickname())
//                // 판매자 정보
//                .sellerId(savedChatRoom.getProduct().getSeller().getUserId())
//                .sellerName(savedChatRoom.getProduct().getSeller().getNickname())
//                .chatMessages(new ArrayList<>())  // 빈 메시지 리스트
//                // 시간 정보
//                .createdAt(savedChatRoom.getCreatedAt())
//                .updatedAt(savedChatRoom.getUpdatedAt())
//                .sellerLeftAt(savedChatRoom.getSellerLeftAt())
//                .buyerLeftAt(savedChatRoom.getBuyerLeftAt())
//                .build();
//    }



    /*채팅 메시지 목록 조회 + 읽음 처리*/
    @Transactional
    public PageResponseDTO<ChatMessageDTO> getChatMessagesAndMarkAsRead(Long chatRoomId, String userId, Pageable pageable) {
        // 1. 채팅방 존재 여부 및 접근 권한 확인
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        // 2. 사용자가 해당 채팅방의 구매자이거나 판매자인지 확인
        if (!chatRoom.isMember(userId)) {
            new AccessDeniedException("해당 채팅방에 접근 권한이 없습니다.");
        }

        // 3. 해당 채팅방의 메시지만 조회 (채팅방 ID로 필터링)
        Page<ChatMessages> chatMsgPage = chatMessageRepository
                .findMessagesSinceLastExit(chatRoomId,userId, pageable);

        // 4. 읽지 않은 메시지 읽음 처리
        List<ChatMessages> unreadMessages = chatMsgPage.getContent().stream()
                .filter(msg -> !msg.isRead() && !msg.getSender().getUserId().equals(userId))
                .collect(Collectors.toList());

        if (!unreadMessages.isEmpty()) {
            unreadMessages.forEach(msg -> msg.UnreadToRead(userId));
            chatMessageRepository.saveAll(unreadMessages);
        }

        // 5. DTO 변환 및 반환
        return new PageResponseDTO<>(chatMsgPage.map(chatMsg ->
                ChatMessageDTO.builder()
                        .chatMessageId(chatMsg.getChatMessageId())
                        .chatRoom(chatRoomId)
                        .sender(chatMsg.getSender().getUserId())
                        .chatMessage(chatMsg.getChatMessage())
                        .createdAt(chatMsg.getCreatedAt())
                        .isRead(chatMsg.isRead())
                        .image(chatMsg.getImage())
                        .build()
        ));
    }
//    public PageResponseDTO<ChatMessageDTO> getChatMessagesAndMarkAsRead(Long chatRoomId,String userId, Pageable pageable) {
//
//        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).get();
//        List<ChatMessages> chatMessages = chatRoom.getChatMessages();
//        //읽지 않은 메시지 읽음 처리 (수신자의 메시지만)
//        chatMessages.stream()
//                .filter(msg -> !msg.isRead())// isRead가 false인 메시지만 필터링
//                .forEach(msg -> msg.UnreadToRead(userId));// unreadToRead() 메소드 호출
//
//        // 변경사항 DB에 반영
//        chatMessageRepository.saveAll(chatMessages);
//
//        //읽음 처리 완료된 상태의 새 메시지 목록 다시 불러오기
//        Page<ChatMessages> chatMsgPage = chatMessageRepository.
//                findMessagesSinceLastExit(chatRoomId,userId, pageable);
//
//        Page<ChatMessageDTO> dtoPage = chatMsgPage.map(chatMsg ->
//                ChatMessageDTO.builder()
//                        .chatMessageId(chatMsg.getChatMessageId())
//                        .chatRoom(chatMsg.getChatRoom().getChatRoomId())  // ChatRoom 객체에서 ID만 추출
//                        .sender(chatMsg.getSender().getUserId())            // User 객체에서 ID만 추출
//                        .chatMessage(chatMsg.getChatMessage())
//                        .createdAt(chatMsg.getCreatedAt())
//                        .isRead(chatMsg.isRead())
//                        .image(chatMsg.getImage())
//                        .build()
//        );
//
//        return new PageResponseDTO<>(dtoPage);
//    }

    /*채팅 메시지 입력 후 전송 눌렀을 때 DB 저장 + 메시지 전송*/
    public ChatMessageDTO sendMessage(Long chatRoomId, ChatMessageDTO messageDTO){

        //1. 채팅방 entity 받아오기
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        // 2. 이전 최신 메시지의 isLatest를 false로 변경
        List<ChatMessages> chatMessages = chatRoom.getChatMessages();
        //읽음 처리
        chatMessages.stream()
                .filter(msg -> msg.isLatest())//isLatest가 true인 메시지만 필터링
                .forEach(msg -> msg.changeNotLatest());//isLatest 값 false로 변경

        // 변경사항 DB에 반영
        chatMessageRepository.saveAll(chatMessages);

        //3. 메시지 entity 생성
        ChatMessages message = ChatMessages.builder()
                .chatRoom(chatRoom)
                .sender(userRepository.findById(messageDTO.getSender())
                        .orElseThrow(()-> new RuntimeException("발신자 없음")))
                .chatMessage(messageDTO.getChatMessage())
                .createdAt(LocalDateTime.now())
                .isRead(false)//새 메시지는 안 읽은 상태
                .isLatest(true)//새 메시지는 최신 메시지
                .type(messageDTO.getType())
                .image(messageDTO.getImage())
                .build();

        // 4. 새 메시지 저장
        ChatMessages savedMessage = chatMessageRepository.save(message);

        ChatMessageDTO chatMessageDTO = ChatMessageDTO.builder()
                .chatRoom(chatRoomId)
                .sender(message.getSender().getUserId())
                .chatMessage(savedMessage.getChatMessage())
                .createdAt(savedMessage.getCreatedAt())
                .isRead(false)//새 메시지는 안 읽은 상태
                .isLatest(true)//새 메시지는 최신 메시지
                .image(savedMessage.getImage())
                .build();
        return chatMessageDTO;
    }

    /**
     * 채팅방 나가기 처리
     * - 해당 사용자의 leftAt 시간을 기록
     * - 시스템 메시지 추가
     * - 채팅방 목록에서 숨김 처리
     */
    public void leaveChatRoom(Long roomId, String userId) throws AccessDeniedException {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        // 나가기 시간 기록
        chatRoom.updateLeftAt(userId);
        // 나감 여부 기록
        chatRoom.updateIsLeft(userId);
        chatRoomRepository.save(chatRoom);

        // 시스템 메시지 저장 -> 추후에 쓸지도?
//        ChatMessages leaveMessage = ChatMessages.builder()
//                .chatRoom(chatRoom)
//                .sender(userRepository.findById(userId).orElseThrow()) //사용자가 보내줄 메시지가 아니라서 필요 없을 듯하나,,user not null 처리 고민해보기
//                .chatMessage(chatRoom.isSeller(userId)
//                        ? "판매자가 채팅방을 나갔습니다."
//                        : "구매자가 채팅방을 나갔습니다.")
//                .type(MessageType.SYSTEM)
//                .createdAt(LocalDateTime.now())
//                .isLatest(true)
//                .isRead(true)
//                .build();
//chatMessageRepository.save(leaveMessage);
//        return ChatMessageDTO.from(leaveMessage);
    }

    @Transactional
    public void markMessageAsRead(Long chatRoomId, String userId, Long messageId) {
        ChatMessages message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("메시지를 찾을 수 없습니다."));

        if (!message.getSender().getUserId().equals(userId)) {
            message.UnreadToRead(userId);
            chatMessageRepository.save(message);
        }
    }

}

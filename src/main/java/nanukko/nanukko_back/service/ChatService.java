package nanukko.nanukko_back.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.chat.ChatMessages;
import nanukko.nanukko_back.domain.chat.ChatRoom;
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

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
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
                findByBuyer_UserIdAndIsBuyerLeavedFalseOrProduct_Seller_UserIdAndIsSellerLeavedFalseOrderByUpdatedAt
                        (userId, userId, pageable);

        // 2단계: Entity를 DTO로 직접 변환
        Page<ChatRoomDTO> dtoPage = chatRoomPage.map(
                chatRoom -> modelMapper.map(chatRoom, ChatRoomDTO.class)
        );

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
            return modelMapper.map(existingChatRoom.get(), ChatRoomDTO.class);
        }

        // 로그인한 사용자(buyer) 조회
        User buyer = userRepository.findById(userId).get(); // 이미 로그인 해서 인증된 사용자이므로 Optional이나 예외처리 불필요

        ChatRoom newChatRoom = ChatRoom.createChatRoom(product,buyer);
        ChatRoom savedChatRoom = chatRoomRepository.save(newChatRoom);//영속성 컨텍스트에 저장(save() 호출로 실제 DB에 저장 + 저장할 때 id값 생성됨)

        return modelMapper.map(savedChatRoom, ChatRoomDTO.class);

    }//createChatRoom

    /*채팅 메시지 목록 조회 + 읽음 처리*/
    @Transactional// 메시지 조회와 읽음 처리를 하나의 트랜잭션으로 처리
    public PageResponseDTO<ChatMessageDTO> getChatMessagesAndMarkAsRead(Long chatRoomId,String userId, Pageable pageable) {

        //읽지 않은 메시지 읽음 처리 (수신자의 메시지만)
        chatMessageRepository.updateIsReadByTrueAndChatRoom_ChatRoomIdAndSender_UserIdNotAndIsReadFalse(chatRoomId, userId);

        Page<ChatMessages> chatMsgPage = chatMessageRepository.
                findByChatRoom_ChatRoomIdOrderByCreatedAt(chatRoomId, pageable);

        Page<ChatMessageDTO> dtoPage = chatMsgPage.map(chatMsg ->
                ChatMessageDTO.builder()
                        .chatMessageId(chatMsg.getChatMessageId())
                        .chatRoom(chatMsg.getChatRoom().getChatRoomId())  // ChatRoom 객체에서 ID만 추출
                        .sender(chatMsg.getSender().getUserId())            // User 객체에서 ID만 추출
                        .chatMessage(chatMsg.getChatMessage())
                        .createdAt(chatMsg.getCreatedAt())
                        .isRead(chatMsg.isRead())
                        .image(chatMsg.getImage())
                        .build()
        );

        return new PageResponseDTO<>(dtoPage);
    }

    /*채팅 메시지 입력 후 전송 눌렀을 때 DB 저장 + 메시지 전송*/
    public ChatMessageDTO saveMessage(Long chatRoomId, ChatMessageDTO messageDTO){

        //1. 채팅방 entity 받아오기
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).get();

        //2. 메시지 entity 생성
        ChatMessages message = ChatMessages.builder()
                .chatRoom(chatRoom)
                .sender(userRepository.findById(messageDTO.getSender())
                        .orElseThrow(()-> new RuntimeException("발신자 없음")))
                .chatMessage(messageDTO.getChatMessage())
                .createdAt(LocalDateTime.now())
                .isRead(false)//새 메시지는 안 읽은 상태
                .isLatest(true)//새 메시지는 최신 메시지
                .image(messageDTO.getImage())
                .build();

        // 3. 이전 최신 메시지의 isLatest를 false로 변경 (선택적)
        //chatMessageRepository.updatePreviousLatestMessage(chatRoomId);

        // 4. 새 메시지 저장
        ChatMessages savedMessage = chatMessageRepository.save(message);

        return null;
    }

}

//
//@Service
//@RequiredArgsConstructor
//@Log4j2
//public class ChatService {
//    private final ChatRoomRepository chatRoomRepository;
//    private final ProductRepository productRepository;
//    private final UserRepository userRepository;
//    private final ModelMapper modelMapper;
//
//    /*채팅방 목록 조회*/
//    public PageResponseDTO<ChatRoomDTO> getChatRooms(String userId, Pageable pageable) {
//        // 1단계: DB에서 페이징된 데이터 조회
//        Page<ChatRoom> chatRoomPage = chatRoomRepository.
//                findByBuyer_UserIdAndIsBuyerLeavedFalseOrProduct_Seller_UserIdAndIsSellerLeavedFalseOrderByUpdatedAt
//                        (userId, userId, pageable);
//
//        // 2단계: Entity를 DTO로 직접 변환
//        Page<ChatRoomDTO> dtoPage = chatRoomPage.map(entity -> {
//            try {
//                return convertToChatRoomDTO(entity);
//            } catch (Exception e) {
//                log.error("Mapping error: ", e);
//                return null;
//            }
//        });
//
//        // 3단계: Page<ChatRoomDTO>를 PageResponseDTO로 포장
//        return new PageResponseDTO<>(dtoPage);
//    }
//
//    /*Entity -> DTO 변환 메서드*/
//    private ChatRoomDTO convertToChatRoomDTO(ChatRoom entity) {
//        List<ChatMessageDTO> messageList = null;
//        if (entity.getChatMessages() != null) {
//            messageList = entity.getChatMessages().stream()
//                    .map(this::convertToChatMessageDTO)
//                    .collect(Collectors.toList());
//        }
//
//        return ChatRoomDTO.builder()
//                .chatRoomId(entity.getChatRoomId())
//                .productId(entity.getProduct().getProductId())
//                .buyerId(entity.getBuyer().getUserId())
//                .sellerId(entity.getProduct().getSeller().getUserId())
//                .chatMessages(messageList)
//                .createdAt(entity.getCreatedAt())
//                .updatedAt(entity.getUpdatedAt())
//                .isSellerLeaved(entity.isSellerLeaved())
//                .isBuyerLeaved(entity.isBuyerLeaved())
//                .build();
//    }
//
//    private ChatMessageDTO convertToChatMessageDTO(ChatMessages message) {
//        return ChatMessageDTO.builder()
//                .chatMessageId(message.getChatMessageId())
//                .sender(message.getSender().getUserId())
//                .chatMessage(message.getChatMessage())
//                .createdAt(message.getCreatedAt())
//                .isRead(message.isRead())
//                .image(message.getImage())
//                .isLatest(message.isLatest())
//                .build();
//    }
//
//    /*새 채팅방 생성*/
//    public ChatRoomDTO createChatRoom(String userId, Long productId){//구매자 아이디, 상품 정보 파라미터로 받음
//
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));
//
//        /*존재하는 채팅방인지 검사*/
//        Optional<ChatRoom> existingChatRoom = chatRoomRepository
//                .findByBuyer_UserIdAndProduct_ProductId(userId, productId);
//        if(existingChatRoom.isPresent()){
//            return modelMapper.map(existingChatRoom.get(), ChatRoomDTO.class);
//        }
//
//        // 로그인한 사용자(buyer) 조회
//        User buyer = userRepository.findById(userId).get(); // 이미 인증된 사용자이므로 Optional이나 예외처리 불필요
//
//        ChatRoom newChatRoom = ChatRoom.createChatRoom(product,buyer);
//        ChatRoom savedChatRoom = chatRoomRepository.save(newChatRoom);//영속성 컨텍스트에 저장(save() 호출로 실제 DB에 저장 + 저장할 때 id값 생성됨)
//
//        return modelMapper.map(savedChatRoom, ChatRoomDTO.class);
//
//    }
//}
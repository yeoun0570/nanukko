package nanukko.nanukko_back.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.order.Orders;
import nanukko.nanukko_back.domain.order.PaymentStatus;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.product.ProductStatus;
import nanukko.nanukko_back.domain.user.Kid;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.domain.user.UserAddress;
import nanukko.nanukko_back.domain.wishlist.Wishlist;
import nanukko.nanukko_back.dto.page.PageResponseDTO;
import nanukko.nanukko_back.dto.user.*;
import nanukko.nanukko_back.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserService {
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final KidRepository kidRepository;
    private final OrderRepository orderRepository;
    private final WishlistRepository wishlistRepository;

    //사용자의 내 정보 조회
    public UserInfoDTO getUserInfo(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        //모든 자녀 조회
        List<Kid> kids = kidRepository.findByUserOrderByKidId(user);
        List<KidInfoDTO> kidInfoDTOS = kids.stream()
                .map(kid -> KidInfoDTO.builder()
                        .kidId(kid.getKidId())
                        .kidBirth(kid.getKidBirth())
                        .kidGender(kid.isKidGender())
                        .build())
                .toList();

        return UserInfoDTO.builder()
                .userId(user.getUserId())
                .password(user.getPassword())
                .userBirth(user.getUserBirth())
                .mobile(user.getMobile())
                .email(user.getEmail())
                .gender(user.isGender())
                .addrMain(user.getAddress().getAddrMain())
                .addrDetail(user.getAddress().getAddrDetail())
                .addrZipcode(user.getAddress().getAddrZipcode())
                .score(user.getScore())
                .profile(user.getProfile())
                .kids(kidInfoDTOS)  // 모든 자녀 정보 포함
                .build();
    }

    // 자녀 정보 업데이트를 위한 별도 메서드
    private void updateKidsInfo(User user, List<KidInfoDTO> kidsDTO) {
        // 기존 자녀 정보 조회
        List<Kid> existingKids = kidRepository.findByUserOrderByKidId(user);

        for (KidInfoDTO kidDTO : kidsDTO) {
            if (kidDTO.getKidId() != null) {
                // 기존 자녀 정보 업데이트
                Kid kid = existingKids.stream()
                        .filter(k -> k.getKidId().equals(kidDTO.getKidId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("자녀 정보를 찾을 수 없습니다."));

                kid.updateInfo(kidDTO.getKidBirth(), kidDTO.isKidGender());
            } else {
                // 새로운 자녀 정보 추가
                Kid newKid = Kid.builder()
                        .user(user)
                        .kidBirth(kidDTO.getKidBirth())
                        .kidGender(kidDTO.isKidGender())
                        .build();
                kidRepository.save(newKid);
            }
        }
    }

    //사용자의 내 정보 수정
    @Transactional
    public UserInfoDTO modifyUserInfo(UserInfoDTO userInfoDTO) {
        //DTO를 VO로 변환
        User user = userRepository.findById(userInfoDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        //기존 엔티티를 DTO로 받은 값으로 업데이트
        user.updateUserInfo(
                userInfoDTO.getMobile(),
                userInfoDTO.getEmail(),
                UserAddress.builder()
                        .addrMain(userInfoDTO.getAddrMain())
                        .addrDetail(userInfoDTO.getAddrDetail())
                        .addrZipcode(userInfoDTO.getAddrZipcode())
                        .build(),
                userInfoDTO.getProfile()
        );
        userRepository.save(user); //@Transactional 이 있다면 생략 가능

        //자녀 정보가 있다면 업데이트
        //DTO를 엔티티로 변환
        if(userInfoDTO.getKids() != null && !userInfoDTO.getKids().isEmpty()) {
            updateKidsInfo(user, userInfoDTO.getKids());
        }

        return modelMapper.map(user, UserInfoDTO.class);
    }

    //사용자의 판매 상품(판매중, 판매완료) 조회
    public PageResponseDTO<UserProductDTO> getSellProducts(String userId, ProductStatus status, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Page<Product> products = productRepository.findBySellerAndStatusOrderByCreatedAtDesc(user, status, pageable);

        Page<UserProductDTO> dtoPage = products.map(product -> UserProductDTO.builder()
                .thumbnailImage(product.getThumbnailImage())
                .productName(product.getProductName())
                .price(product.getPrice())
                .status(product.getStatus())
                .build());

        return new PageResponseDTO<>(dtoPage);
    }

    //사용자의 상품 상태 변경

    //사용자의 구매 상품(구매중, 구매완료) 조회
    public PageResponseDTO<UserOrderDTO> getOrderProducts(String userId, PaymentStatus status, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Page<Orders> orders = orderRepository.findByBuyerAndStatusOrderByCreatedAtDesc(user, status, pageable);

        Page<UserOrderDTO> dtoPage = orders.map(order -> UserOrderDTO.builder()
                .thumbnailImage(order.getProduct().getThumbnailImage())
                .productName(order.getProduct().getProductName())
                .price(order.getProductAmount())
                .status(order.getStatus())
                .build());

        return new PageResponseDTO<>(dtoPage);
    }

    //찜 목록 조회
    public PageResponseDTO<UserWishlistDTO> getWishlists(String userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Page<Wishlist> wishlists = wishlistRepository.findByUser(user, pageable);

        Page<UserWishlistDTO> dtoPage = wishlists.map(wishlist -> UserWishlistDTO.builder()
                .thumbnailImage(wishlist.getProduct().getThumbnailImage())
                .price(wishlist.getProduct().getPrice())
                .productName(wishlist.getProduct().getProductName())
                .status(wishlist.getProduct().getStatus())
                .build());

        return new PageResponseDTO<>(dtoPage);
    }

    //찜 목록에서 제거

    //탈퇴하기

    //후기 작성은 상품에서 해야되나?
}

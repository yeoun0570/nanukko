package nanukko.nanukko_back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.order.Orders;
import nanukko.nanukko_back.domain.order.PaymentStatus;
import nanukko.nanukko_back.domain.product.Image;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.product.ProductStatus;
import nanukko.nanukko_back.domain.product.category.MiddleCategory;
import nanukko.nanukko_back.domain.review.Review;
import nanukko.nanukko_back.domain.user.Kid;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.domain.user.UserAddress;
import nanukko.nanukko_back.domain.wishlist.Wishlist;
import nanukko.nanukko_back.dto.file.FileDTO;
import nanukko.nanukko_back.dto.file.FileDirectoryType;
import nanukko.nanukko_back.dto.page.PageResponseDTO;
import nanukko.nanukko_back.dto.product.ProductRequestDto;
import nanukko.nanukko_back.dto.review.ReviewInMyStoreDTO;
import nanukko.nanukko_back.dto.review.ReviewRegisterDTO;
import nanukko.nanukko_back.dto.user.*;
import nanukko.nanukko_back.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

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
    private final MiddleCategoryRepository middleCategoryRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationService notificationService;
    private final ImageService imageService;

    //사용자의 내 정보 조회
    @Transactional(readOnly = true)
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
                .nickname(user.getNickname())
                .password(user.getPassword())
                .userBirth(user.getUserBirth())
                .mobile(user.getMobile())
                .email(user.getEmail())
                .gender(user.isGender())
                .addrMain(user.getAddress().getAddrMain())
                .addrDetail(user.getAddress().getAddrDetail())
                .addrZipcode(user.getAddress().getAddrZipcode())
                .reviewRate(user.getReviewRate())
                .profile(user.getProfile())
                .kids(kidInfoDTOS)  // 모든 자녀 정보 포함
                .build();
    }

    //사용자의 내 정보 수정
    @Transactional
    public UserInfoDTO modifyUserInfo(UserInfoDTO userInfoDTO) {
        // 기본 사용자 정보 업데이트와 자녀 정보 업데이트를 분리
        User updatedUser = updateUserBasicInfo(userInfoDTO);

        if (userInfoDTO.getKids() != null) {
            updateKidsInfo(updatedUser, userInfoDTO.getKids());
        }

        return modelMapper.map(updatedUser, UserInfoDTO.class);
    }

    //사용자 정보 업데이트 별도의 트랜잭션으로 관리(커넥션 풀 최적화)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected User updateUserBasicInfo(UserInfoDTO userInfoDTO) {
        try {
            //DTO를 VO로 변환
            User user = userRepository.findById(userInfoDTO.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            //기존 엔티티를 DTO로 받은 값으로 업데이트
            user.updateUserInfo(
                    userInfoDTO.getNickname(),
                    userInfoDTO.getMobile(),
                    userInfoDTO.getEmail(),
                    UserAddress.builder()
                            .addrMain(userInfoDTO.getAddrMain())
                            .addrDetail(userInfoDTO.getAddrDetail())
                            .addrZipcode(userInfoDTO.getAddrZipcode())
                            .build(),
                    userInfoDTO.getProfile()
            );
            return userRepository.save(user); //@Transactional 이 있다면 생략 가능
        } catch (Exception e) {
            log.error("유저 정보 업데이트 실패 - userId: {}, error: {}",
                    userInfoDTO.getUserId(), e.getMessage());
            throw e;
        }
    }

    //자녀 정보 업데이트 별도의 트랜잭션으로 관리(커넥션 풀 최적화)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void updateKidsInfo(User user, List<KidInfoDTO> kidsDTO) {
        try {
            for (KidInfoDTO kidDTO : kidsDTO) {
                if (kidDTO.getKidId() == null) {
                    //새로운 자녀 ID 생성
                    String newKidId = generateKidId(user);
                    Kid kid = Kid.builder()
                            .user(user)
                            .kidId(newKidId)
                            .kidBirth(kidDTO.getKidBirth())
                            .kidGender(kidDTO.isKidGender())
                            .build();
                    kidRepository.save(kid);
                } else {
                    // 기존 자녀 정보 업데이트
                    Kid kid = kidRepository.findByUserAndKidId(user, kidDTO.getKidId())
                            .orElseThrow(() -> new IllegalArgumentException("자녀 정보를 찾을 수 없습니다."));
                    kid.updateInfo(kidDTO.getKidBirth(), kidDTO.isKidGender());
                    kidRepository.save(kid);
                }
            }
        } catch (Exception e) {
            log.error("자녀 정보 업데이트 실패 - userId: {}, error: {}",
                    user.getUserId(), e.getMessage());
            throw e;
        }
    }

    // 새로운 kidId 생성 메서드
    private String generateKidId(User user) {
        //사용자의 자녀 목록 찾기
        List<Kid> existingKids = kidRepository.findByUserOrderByKidId(user);
        //nextNum 선언
        int nextNum = existingKids.size() + 1;
        //KidId로 사용할 String 값 생성
        return String.format("%s_KID_%d", user.getUserId(), nextNum);
    }

    //사용자 배송지 정보 수정
    @Transactional
    public UserAddrDTO modifyUserAddr(String userId, UserAddrDTO addrDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.updateUserAddr(UserAddress.builder()
                        .addrMain(addrDTO.getAddrMain())
                        .addrDetail(addrDTO.getAddrDetail())
                        .addrZipcode(addrDTO.getAddrZipcode())
                .build());
        userRepository.save(user);

        return modelMapper.map(user, UserAddrDTO.class);
    }

    //사용자의 판매 상품(판매중, 판매완료) 조회
    @Transactional(readOnly = true)
    public PageResponseDTO<UserProductDTO> getSellProducts(String userId, ProductStatus status, Pageable pageable) {
        log.info("Fetching products - userId: {}, status: {}", userId, status);
        log.info("Pageable info - page: {}, size: {}, offset: {}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getOffset());  // offset 로깅 추가

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Page<Product> products = productRepository.findBySellerAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(user, status, pageable);
        log.info("Query result - total elements: {}, total pages: {}, current page: {}",
                products.getTotalElements(),
                products.getTotalPages(),
                products.getNumber());

        Page<UserProductDTO> dtoPage = products.map(product -> UserProductDTO.builder()
                .productId(product.getProductId())
                .thumbnailImage(product.getThumbnailImage())
                .productName(product.getProductName())
                .price(product.getPrice())
                .status(product.getStatus())
                .hasDelivery(product.isHasDelivery())
                .build());

        return new PageResponseDTO<>(dtoPage);
    }

    //사용자의 판매 상품 수정
    @Transactional
    public Product modifyProduct(String userId, ProductRequestDto dto, List<MultipartFile> images) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));


        Product product = productRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        //상품의 판매자와 요청한 사용자가 같은지 확인
        if (!product.getSeller().getUserId().equals(userId)) {
            throw new IllegalArgumentException("상품 수정 권한이 없습니다.");
        }

        //클라우드에 등록된 사진 삭제
        List<String> fileUrls = Stream.of(
                        product.getImages().getImage1(),
                        product.getImages().getImage2(),
                        product.getImages().getImage3(),
                        product.getImages().getImage4(),
                        product.getImages().getImage5()
                )
                .filter(url -> url != null) // null 제거
                .toList();

        imageService.deleteFilesByUrls(fileUrls);

        // 중분류 카테고리 조회, 카테고리 업데이트
        MiddleCategory middleCategory = middleCategoryRepository.findById(dto.getMiddleId()).orElseThrow(() -> new IllegalArgumentException("카테고리 찾기 실패"));
        product.updateCategory(middleCategory);

        //상품 정보 수정
        product.updateProduct(dto);

        //사진 업로드
        List<FileDTO> imgUrls = imageService.uploadMultipleFiles(images, FileDirectoryType.SELL, userId);
        log.info("업로드 이미지 url : {}" , imgUrls);
        Image image = new Image(imgUrls);
        product.setImages(image);
        product.setThumbnailImage(image.getImage1());

        productRepository.save(product);
        return product;
    }

    //사용자의 판매 상품 삭제
    @Transactional
    public UserRemoveProductDTO removeProduct(String userId, Long productId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));


        if (product.getStatus() != ProductStatus.RESERVED) {
            product.removeProduct(true);
        } else {
            throw new IllegalArgumentException("예약중인 상품은 삭제할 수 없습니다.");
        }

        return UserRemoveProductDTO.builder()
                .isDeleted(product.isDeleted())
                .status(ProductStatus.REMOVED)
                .build();
    }

    //사용자의 구매 상품(구매중, 구매완료) 조회
    @Transactional(readOnly = true)
    public PageResponseDTO<UserOrderDTO> getOrderProducts(String userId, PaymentStatus status, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Page<Orders> orders = orderRepository.findByBuyerAndStatusAndProductIsDeletedFalseOrderByCreatedAtDesc(user, status, pageable);

        Page<UserOrderDTO> dtoPage = orders.map(order -> UserOrderDTO.builder()
                .orderId(order.getOrderId())
                .thumbnailImage(order.getProduct().getThumbnailImage())
                .productName(order.getProduct().getProductName())
                .price(order.getProductAmount())
                .status(order.getStatus())
                .build());

        return new PageResponseDTO<>(dtoPage);
    }

    //찜 목록 조회
    @Transactional(readOnly = true)
    public PageResponseDTO<UserWishlistDTO> getWishlists(String userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Page<Wishlist> wishlists = wishlistRepository.findByUser(user, pageable);

        Page<UserWishlistDTO> dtoPage = wishlists.map(wishlist -> UserWishlistDTO.builder()
                .productId(wishlist.getProduct().getProductId())
                .thumbnailImage(wishlist.getProduct().getThumbnailImage())
                .price(wishlist.getProduct().getPrice())
                .productName(wishlist.getProduct().getProductName())
                .status(wishlist.getProduct().getStatus())
                .build());

        return new PageResponseDTO<>(dtoPage);
    }

    //찜 목록에서 제거
    @Transactional
    public void removeWishlist(String userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        wishlistRepository.deleteWishlistByUserAndProduct(user, product);
    }

    //후기 작성
    @Transactional
    public ReviewRegisterDTO writeReview(String userId, ReviewRegisterDTO reviewDTO) {
        Review savedReview = saveReview(userId, reviewDTO);
        updateSellerRating(savedReview.getProduct().getSeller());
        return ReviewRegisterDTO.builder()
                .rate(savedReview.getRate())
                .review(savedReview.getReview())
                .build();
    }

    //거래 후기 저장 별도의 트랜잭션으로 관리(커넥션 풀 최적화)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected Review saveReview(String userId, ReviewRegisterDTO reviewDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Orders order = orderRepository.findById(reviewDTO.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        //후기 DTO에서 VO로 변환해서 저장
        Review review = Review.builder()
                .user(user)
                .product(order.getProduct())
                .rate(reviewDTO.getRate())
                .createdAt(LocalDateTime.now())
                .review(reviewDTO.getReview())
                .build();

        //판매자에게 리뷰 작성 알림 전송
        notificationService.sendConfirmReview(
                order.getProduct().getSeller().getUserId(), user.getNickname()
        );

        return reviewRepository.save(review);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void updateSellerRating(User seller) {
        //후기 작성하고 판매자의 상점 평점 평균 계산하기(사용자의 총 점수 / 사용자의 총 리뷰 수)
        double averageScore = reviewRepository.averageRateByProductSeller(seller);
        seller.updateReviewRate(averageScore);
        userRepository.save(seller);
    }

    // 이미 작성된 후기인지 체크
    @Transactional(readOnly = true)
    public boolean checkReviewExists(String orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        return reviewRepository.existsByProduct(order.getProduct());
    }


    //후기 조회
    @Transactional(readOnly = true)
    public PageResponseDTO<ReviewInMyStoreDTO> getReview(String userId, Pageable pageable) {
        User seller = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        //판매자가 받은 후기 조회
        Page<Review> reviews = reviewRepository.findByProductSellerOrderByCreatedAtDesc(seller, pageable);

        // DTO로 변환
        Page<ReviewInMyStoreDTO> reviewPage = reviews.map(review -> ReviewInMyStoreDTO.builder()
                .reviewId(review.getReviewId())
                .productId(review.getProduct().getProductId())
                .authorId(review.getUser().getUserId())  // 후기 작성자 ID
                .authorNickName(review.getUser().getNickname()) // 후기 작성자 이름
                .rate(review.getRate())
                .productName(review.getProduct().getProductName())
                .thumbnail(review.getProduct().getThumbnailImage()) // 후기 작성된 썸네일 이미지
                .review(review.getReview())
                .reviewRate(seller.getReviewRate())  // 판매자의 전체 평점
                .build());

        return new PageResponseDTO<>(reviewPage);
    }

    //탈퇴하기
    @Transactional
    public UserRemoveDTO removeUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이미 탈퇴한 회원인지 확인
        if (user.isCanceled()) {
            throw new IllegalStateException("이미 탈퇴한 회원입니다.");
        }

        // 진행중인 거래(에스크로 보관 중) 확인
        boolean hasActiveBuyingOrders = orderRepository.existsByBuyerAndStatus(user, PaymentStatus.ESCROW_HOLDING);
        boolean hasActiveSellingOrders = orderRepository.existsByProductSellerAndStatus(user, PaymentStatus.ESCROW_HOLDING);
        if (hasActiveBuyingOrders || hasActiveSellingOrders) {
            throw new IllegalStateException("진행중인 거래가 있어 탈퇴할 수 없습니다. 에스크로 보관 중인 거래를 먼저 완료해주세요.");
        }

        user.cancelUser();
        userRepository.save(user);

        return UserRemoveDTO.builder()
                .userId(user.getUserId())
                .canceled(user.isCanceled())
                .build();
    }

    //사이드바에 있는 프로필 정보
    @Transactional(readOnly = true)
    public UserSimpleInfoDTO getSimpleInfo(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        int countProduct = productRepository.countBySeller(user);

        return UserSimpleInfoDTO.builder()
                .profile(user.getProfile())
                .nickname(user.getNickname())
                .reviewRate(user.getReviewRate())
                .countProduct(countProduct)
                .build();
    }

    //문의 조회 --> 챗봇 진행되고 할 예정
}

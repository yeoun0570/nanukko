package nanukko.nanukko_back.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.log.ActionType;
import nanukko.nanukko_back.domain.log.UserActionLog;
import nanukko.nanukko_back.domain.product.Condition;
import nanukko.nanukko_back.domain.product.Image;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.product.category.MiddleCategory;
import nanukko.nanukko_back.domain.user.Kid;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.dto.file.FileDTO;
import nanukko.nanukko_back.dto.file.FileDirectoryType;
import nanukko.nanukko_back.dto.page.PageResponseDTO;
import nanukko.nanukko_back.dto.product.ProductRequestDto;
import nanukko.nanukko_back.dto.product.ProductResponseDto;
import nanukko.nanukko_back.repository.*;
import nanukko.nanukko_back.repository.log.UserActionLogRepository;
import nanukko.nanukko_back.util.ProductMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private final KidRepository kidRepository;
    private final MiddleCategoryRepository middleCategoryRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WishlistRepository wishlistRepository;
    private final ImageService imageService;
    private final UserActionLogRepository userActionLogRepository;

    public PageResponseDTO<ProductResponseDto> getMainProducts(Pageable pageable) { //미로그인 사용자 상품 리스트 (최근 등록 순)
        Page<Product> result = productRepository.findAllByIsDeletedFalse(pageable);
        Page<ProductResponseDto> dto = result.map(ProductMapper::toDto);
        return new PageResponseDTO<>(dto);
    }

    public PageResponseDTO<ProductResponseDto> getMainProducts(Pageable pageable, String username) { //로그인 사용자 추천 상품 리스트
        log.info("=== 사용자 추천 시작 ===");
        //부모 객체 조회
        User user = userRepository.findById(username).orElseThrow(() -> new IllegalArgumentException("사용자 찾기 실패"));
        //사용자의 자녀 정보 가져오기
        List<Kid> kids = kidRepository.findByUserOrderByKidId(user);
        //자녀 정보가 1개 이상인지 확인
        log.info("자녀 수 : " + kids.size());

        if (kids.isEmpty()) {
            //자녀 정보가 없을 때 -> 일반 조회
            return getMainProducts(pageable);
        }
        //자녀 정보가 있을 때 연령대 계산 (쌍둥이의 경우 중복 결과를 방지하기 위해 Set으로 설정 )
        Map<Integer, Set<Kid>> groupToKidsMap = new HashMap<>();

        for (Kid kid : kids) {
            int ageGroup = calculateAgeGroup(kid.getKidBirth(), kid.isKidGender());
            groupToKidsMap.computeIfAbsent(ageGroup, k -> new HashSet<>()).add(kid);
        }

        List<ProductResponseDto> recommendedProducts = new ArrayList<>(); //추천 상품 리스트
        long totalElements = 0;

        for (Map.Entry<Integer, Set<Kid>> entry : groupToKidsMap.entrySet()) {
            int ageGroup = entry.getKey();
            Page<Long> popularProductIds = userActionLogRepository.findPopularProductIdsByAgeGroup(ageGroup, pageable);
            List<Product> products = productRepository.findAllById(popularProductIds.getContent());
            products.stream()
                    .map(ProductMapper::toDto)
                    .forEach(recommendedProducts::add);

            totalElements += popularProductIds.getTotalElements();
        }

        Page<ProductResponseDto> combinedPage = new PageImpl<>(recommendedProducts, pageable, totalElements);
        return new PageResponseDTO<>(combinedPage);
    }


    //자녀 연령대 분류 메서드
    public int calculateAgeGroup(LocalDate birth, boolean gender) {
        LocalDate today = LocalDate.now(); //오늘 날짜
        int months = Period.between(birth, today).getYears() * 12 + Period.between(birth, today).getMonths();

        int ageGroup;
        if (months <= 3) {
            ageGroup = 1;
        } else if (months <= 6) {
            ageGroup = 2;
        } else if (months <= 12) {
            ageGroup = 3;
        } else if (months <= 24) {
            ageGroup = 4;
        } else if (months <= 36) {
            ageGroup = 5;
        } else if (months <= 48) {
            ageGroup = 6;
        } else if (months <= 60) {
            ageGroup = 7;
        } else {
            ageGroup = 8;
        }

        // 성별에 따라 그룹 ID 조정
        return gender ? ageGroup * 2 : ageGroup * 2 - 1; // 남아: 짝수, 여아: 홀수
    }

    // 현재 로그인한 사용자 조회
    public Product createProduct(ProductRequestDto dto, List<MultipartFile> images, String userId) {
        User seller = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자 찾기 실패"));

        // 중분류 카테고리 조회
        MiddleCategory middleCategory = middleCategoryRepository.findById(dto.getMiddleId()).orElseThrow(() -> new IllegalArgumentException("카테고리 찾기 실패"));

        //이미지 업로드, URL 리스트
        List<FileDTO> imgUrls = imageService.uploadMultipleFiles(images, FileDirectoryType.SELL, seller.getUserId());

        log.info("업로드 이미지 url : {}", imgUrls);
        Image image = new Image(imgUrls);

        //상품 엔티티 생성
        Product product = Product.builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .seller(seller)
                .productName(dto.getProductName())
                .price(dto.getPrice())
                .content(dto.getContent())
                .images(image)
                .thumbnailImage(image.getImage1())
                .condition(Condition.valueOf(dto.getCondition()))
                .middleCategory(middleCategory)
                .isPerson(dto.getIsPerson())
                .isShipping(dto.getIsShipping())
                .freeShipping(dto.getFreeShipping())
                .shippingFee(dto.getShippingFee())
                .isCompanion(dto.getIsCompanion())
                .isDeputy(dto.getIsDeputy())
                .gender(dto.getGender())
                .ageGroup(dto.getAgeGroup())
                .zipCode(dto.getZipCode())
                .address(dto.getAddress())
                .detailAddress(dto.getDetailAddress())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();

        return productRepository.save(product);
    }

    public ProductResponseDto getProductDetail(Long productId, User user) {
        Product product = getProductById(productId);
        boolean isWished = user != null && wishlistRepository.existsByUserAndProduct(user, product);
        ProductResponseDto dto = ProductMapper.toDto(product);
        dto.setIsWished(isWished);

        //Click 이벤트 로그 저장
        if (user != null) {
            saveLog(user, product);
        }

        return dto;
    }

    @Cacheable(
            value = "productSearch",
            key = "#query + '_' + #pageable.pageNumber + '_' + #pageable.pageSize",
            condition = "#query.length() >= 2",
            unless = "#result.totalElements == 0"
    )
    public PageResponseDTO<Product> searchProducts(String query, Pageable pageable, User user) {//상품명 검색 페이지별 조회
        Page<Product> searchResult = productRepository.searchByName(query, pageable);
        if (user != null) {
            saveLog(user, null);
        }

        return new PageResponseDTO<>(searchResult);
    }

    @Scheduled(cron = "0 0 0 * * SUN")
    @CacheEvict(value = "productSearch", allEntries = true)
    public void clearProductCache() {
        // 매주 일요일 자정에 저장된 캐시 전체 삭제
    }

    public PageResponseDTO<Product> findByMajorCategory(Long majorId, Pageable pageable) { //대분류별 상품 조회
        Page<Product> result = productRepository.findByMajorId(majorId, pageable);
        return new PageResponseDTO<>(result);
    }

    public PageResponseDTO<Product> findByMiddleCategory(Long middleId, Pageable pageable) { //중분류별 상품 조회
        Page<Product> result = productRepository.findByMiddleId(middleId, pageable);
        return new PageResponseDTO<>(result);
    }

    public List<ProductResponseDto> findRelatedProducts(Long productId) {
        // 1. 현재 상품의 중분류 ID 조회
        Product currentProduct = getProductById(productId);
        Long middleId = currentProduct.getMiddleCategory().getMiddleId();
        log.info("연관상품 조회 중..." + middleId);
        // 2. 동일한 중분류의 다른 상품들 조회
        List<Product> products = productRepository.findRelatedProducts(productId, middleId);
        log.info("연관상품 조회 중...List<Product> 몇 개? : " + products.size());

        List<ProductResponseDto> dto = products.stream()
                .map(ProductMapper::toDto) // 각 Product를 ProductResponseDto로 변환
                .toList();
        log.info("연관 상품 Dto : " + dto);
        return dto;
    }

    ///////////////////공통 메서드//////////////////////
    private Product getProductById(Long productId) { //공통 메서드
        return productRepository.findById(productId).orElseThrow(() -> new EntityNotFoundException("해당 상품을 찾을 수 없음"));
    }

    ////////////////로그 메서드//////////////////////
    private void saveLog(User user, Product product) {
        // 자녀 정보 가져오기
        List<Kid> kids = kidRepository.findByUserOrderByKidId(user);

        if (!kids.isEmpty()) {
            // 모든 자녀에 대해 로그 생성 및 저장
            for (Kid kid : kids) {
                int ageGroup = calculateAgeGroup(kid.getKidBirth(), kid.isKidGender());

                UserActionLog log = new UserActionLog();
                log.setUserId(user.getUserId());
                log.setProduct(product);
                log.setActionType(ActionType.CLICK);
                log.setUpdatedAt(LocalDateTime.now());
                log.setAgeGroup(ageGroup);

                userActionLogRepository.save(log);
            }
        }
    }


}
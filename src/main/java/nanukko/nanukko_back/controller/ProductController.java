package nanukko.nanukko_back.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.dto.user.UserSetProductDTO;
import nanukko.nanukko_back.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/products")
public class ProductController {
    private final UserService userService;
/*
    @PostMapping("/new")
    public ResponseEntity<?> createNewProduct (
            @RequestBody()


                                        @RequestParam("file") MultipartFile file, //******
                                        @RequestParam("reviewContents") String reviewContents,
                                        @RequestParam("reservationId") Long reservationId,
                                        @RequestParam("registerDate") String registerDate,
                                        @RequestParam("reviewScore") Long reviewScore,
                                        @RequestParam("userEmail") String userEmail) {
        try {
            // *******
            List<FileDTO> fileDTOList = fileService.uploadFiles(List.of(file), "reviews");
            String img = fileDTOList.get(0).getUploadFileUrl();

            // Save review details
            ReviewRequest reviewRequest = ReviewRequest.builder()
                    .reviewContents(reviewContents)
                    .reservationId(reservationId)
                    .registerDate(LocalDate.parse(registerDate))
                    .reviewScore(reviewScore)
                    .userEmail(userEmail)
                    .img(img)
                    .build();

            myDiningService.registerReview(reviewRequest);

            return ResponseEntity.ok("File uploaded and review saved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
    }*/

    /*// 현재 이미지를 varchar(255)로 받아서 이미지 크기를 담을 수가 없어서 생성이 안됨. 나중에 클라우드 도입시키고 새로 짜야될거 같음
    @PostMapping("/product/register")
    public ResponseEntity<?> registerProduct(
            @RequestParam String userId,
            @RequestBody UserSetProductDTO productDTO
    ) {
        try {
            log.info("Received userId: {}", userId);
            log.info("Received productDTO: {}", productDTO);

            UserSetProductDTO savedProduct = userService.registerProduct(userId, productDTO);
            return ResponseEntity.ok(savedProduct);
        } catch (Exception e) {
            log.error("Error registering product", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }*/

}

package nanukko.nanukko_back.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.product.Image;
import nanukko.nanukko_back.service.FileService;
import nanukko.nanukko_back.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Log4j2
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {

    private final FileService fileService;

    @GetMapping("/test")
    public String getTest() {
        log.info("제발 보내져라..........................");
        return "백엔드에서 데이터 보내기 테스트";
    }

    @PostMapping("/test/products/new")
    public ResponseEntity<Void> uploadProductImg (@RequestParam("images") List<MultipartFile> images) {
        try {
            List<String> uploadedUrls = fileService.uploadProductImages(images, "sell", 500);
            log.info("URL 저장 OK? " + uploadedUrls.toString());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/test/my-page/profile")
    public ResponseEntity<Void> uploadProfile (@RequestParam("image") MultipartFile image) {
        try {
            String uploadedUrls = fileService.uploadProfile(image, "profile", 200);
            log.info("URL 저장 OK? " + uploadedUrls.toString());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}

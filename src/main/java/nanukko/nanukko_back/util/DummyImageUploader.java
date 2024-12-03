package nanukko.nanukko_back.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.product.Image;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.dto.file.FileDTO;
import nanukko.nanukko_back.dto.file.FileDirectoryType;
import nanukko.nanukko_back.repository.ProductRepository;
import nanukko.nanukko_back.service.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class DummyImageUploader {
    private final ImageService imageService;
    private final ProductRepository productRepository;

    private static final String DUMMY_IMAGES_PATH = "src/test/resources/dummy-images/";

    private static class DummyMultipartFile implements MultipartFile {
        private final File file;
        private final String contentType;
        private final String originalFilename;

        public DummyMultipartFile(File file, String contentType) {
            this.file = file;
            this.contentType = contentType;
            this.originalFilename = file.getName();
        }

        @Override
        public String getName() {
            return file.getName();
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return file.length() == 0;
        }

        @Override
        public long getSize() {
            return file.length();
        }

        @Override
        public byte[] getBytes() throws IOException {
            return Files.readAllBytes(file.toPath());
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new FileInputStream(file);
        }

        @Override
        public void transferTo(File dest) throws IOException {
            Files.copy(file.toPath(), dest.toPath());
        }
    }

    // 카테고리에 맞는 이미지 파일들만 필터링하여 반환
    private List<File> getMatchingImageFiles(String categoryName) throws IOException {
        File dummyImagesDir = new File(DUMMY_IMAGES_PATH);
        File[] allImageFiles = dummyImagesDir.listFiles((dir, name) ->
                (name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".jpeg") ||
                        name.toLowerCase().endsWith(".png")) &&
                        name.toLowerCase().startsWith(categoryName.toLowerCase()));

        if (allImageFiles == null || allImageFiles.length == 0) {
            log.warn("No matching images found for category: {}", categoryName);
            return List.of();
        }

        List<File> matchingFiles = new ArrayList<>(List.of(allImageFiles));
        Collections.shuffle(matchingFiles); // 랜덤 순서로 섞기
        return matchingFiles;
    }

    // 단일 상품의 이미지 업로드 (카테고리 기반)
    public List<FileDTO> uploadImagesForProduct(Product product) throws IOException {
        String categoryName = product.getMiddleCategory().getMiddleName();
        List<File> matchingFiles = getMatchingImageFiles(categoryName);

        if (matchingFiles.isEmpty()) {
            log.error("No matching images found for category: {}", categoryName);
            return List.of();
        }

        // 파일들을 MultipartFile로 변환
        List<MultipartFile> multipartFiles = matchingFiles.stream()
                .map(file -> {
                    try {
                        String contentType = Files.probeContentType(file.toPath());
                        return new DummyMultipartFile(file, contentType);
                    } catch (IOException e) {
                        log.error("Failed to create MultipartFile for: {}", file.getName(), e);
                        return null;
                    }
                })
                .filter(file -> file != null)
                .collect(Collectors.toList());

        return imageService.uploadMultipleFiles(multipartFiles, FileDirectoryType.SELL, product.getSeller().getUserId());
    }

    // 모든 상품의 이미지 업로드
    @Transactional
    public void uploadImagesForAllProducts() {
        var products = productRepository.findAll();
        log.info("Found {} products to update", products.size());

        for (var product : products) {
            try {
                List<FileDTO> uploadedFiles = uploadImagesForProduct(product);

                if (!uploadedFiles.isEmpty()) {
                    Image productImages = new Image(uploadedFiles);
                    product.setImages(productImages);
                    product.setThumbnailImage(uploadedFiles.get(0).getUploadFileUrl());
                    productRepository.save(product);
                    log.info("Updated images for product: {} with category: {}",
                            product.getProductId(),
                            product.getMiddleCategory().getMiddleName());
                } else {
                    log.warn("No images uploaded for product: {} with category: {}",
                            product.getProductId(),
                            product.getMiddleCategory().getMiddleName());
                }

            } catch (IOException e) {
                log.error("Failed to upload images for product: {}", product.getProductId(), e);
            }
        }
    }
}
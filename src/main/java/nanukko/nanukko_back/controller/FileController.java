package nanukko.nanukko_back.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.dto.file.FileDTO;
import nanukko.nanukko_back.dto.file.FileDirectoryType;
import nanukko.nanukko_back.dto.user.CustomUserDetails;
import nanukko.nanukko_back.service.ImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
@Log4j2
public class FileController {
    private final ImageService imageService;

    // 단일 파일 URL 조회
    @GetMapping("/{filePath}/{fileName}")
    public ResponseEntity<String> getFileUrl(
            @PathVariable String filePath,
            @PathVariable String fileName
    ) {
        log.info("파일 URL 조회 요청 - 경로: {}, 파일명: {}", filePath, fileName);
        String fileUrl = imageService.getFileUrl(filePath, fileName);
        return ResponseEntity.ok(fileUrl);
    }

    //프로필 이미지 업로드(프로필은 한장이기 때문에 단일 업로드로 진행)
    @PostMapping("profile")
    public ResponseEntity<FileDTO> uploadProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file
    ) {
        String userId = userDetails.getUsername();
        log.info("프로필 이미지 업로드 요청: {}", file.getOriginalFilename());
        FileDTO fileDTO = imageService.uploadSingleFile(file, FileDirectoryType.PROFILE, userId);
        return ResponseEntity.ok(fileDTO);
    }

    // 채팅 이미지 다중 업로드
    @PostMapping("/chat/multiple")
    public ResponseEntity<List<FileDTO>> uploadChatImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("files") List<MultipartFile> files
    ) {
        String userId = userDetails.getUsername();
        log.info("채팅 이미지 다중 업로드 요청: {} 개", files.size());
        List<FileDTO> fileDTOs = imageService.uploadMultipleFiles(files, FileDirectoryType.CHAT, userId);
        return ResponseEntity.ok(fileDTOs);
    }

    // 판매 이미지 다중 업로드 (상품 이미지 여러장)
    @PostMapping("/sell/multiple")
    public ResponseEntity<List<FileDTO>> uploadSellImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("files") List<MultipartFile> files
    ) {
        String userId = userDetails.getUsername();
        log.info("판매 이미지 다중 업로드 요청: {} 개", files.size());
        List<FileDTO> fileDTOs = imageService.uploadMultipleFiles(files, FileDirectoryType.SELL, userId);
        return ResponseEntity.ok(fileDTOs);
    }

    // 파일 삭제
    @DeleteMapping("/{filePath}/{fileName}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable String filePath,
            @PathVariable String fileName
    ) {
        log.info("파일 삭제 요청 - 경로: {}, 파일명: {}", filePath, fileName);
        imageService.deleteFile(filePath, fileName);
        return ResponseEntity.ok().build();
    }

    // 다중 파일 삭제
    @DeleteMapping("/multiple")
    public ResponseEntity<Void> deleteFiles(@RequestBody List<FileDTO> files) {
        log.info("다중 파일 삭제 요청: {} 개", files.size());
        imageService.deleteFiles(files);
        return ResponseEntity.ok().build();
    }
}

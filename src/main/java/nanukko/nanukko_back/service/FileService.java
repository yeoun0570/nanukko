package nanukko.nanukko_back.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.dto.FileDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import net.coobird.thumbnailator.Thumbnails;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class FileService {

    private final AmazonS3Client amazonS3Client;

    @Value("${spring.s3.bucket}")
    private String bucketName;

    //500X500 업로드
    //300X300 채팅방 이미지
    //200X200 프로필 이미지

    public List<String> uploadProductImages(List<MultipartFile> files, String folderPath, int size) {
        List<String> uploadedUrls = new ArrayList<>(); //업로드 URL 리스트

        for (MultipartFile file : files) {
            log.info("-------------파일 업로드 시작-------------");

            try {
                log.info("Cloud 연결 시도");
                amazonS3Client.listBuckets().forEach(bucket -> System.out.println("buckets : " + bucket.getName()));
            } catch (Exception e) {
                System.err.println("Error connecting to S3: " + e.getMessage());
            }

            //파일 타입 체크
            if (checkContentType(file)) continue;

            try (InputStream input = file.getInputStream()) {
                //파일 이름 및 경로 설정
                String fileName = UUID.randomUUID().toString(); //파일 이름 생성
                String filePath = folderPath + "/" + fileName;

                //원본 이미지 압축
                ByteArrayOutputStream compressedImageStream = new ByteArrayOutputStream(); //메모리 내에서 처리

                Thumbnails.of(input)
                        .size(size, size) //이미지 크기
                        .outputFormat("jpg") //압축 형식
                        .toOutputStream(compressedImageStream);


                //클라우드 저장
                ObjectMetadata metadata = new ObjectMetadata(); //메타데이터 설정
                metadata.setContentLength(compressedImageStream.size()); //사이즈
                metadata.setContentType("image/jpeg"); //파일 타입

                try (InputStream compressedInputStream = new ByteArrayInputStream(compressedImageStream.toByteArray())) {
                    amazonS3Client.putObject(
                            new PutObjectRequest(bucketName, filePath, compressedInputStream, metadata)
                                    .withCannedAcl(CannedAccessControlList.PublicReadWrite)
                    );
                }

                // 업로드된 파일 URL 저장
                String uploadedUrl = amazonS3Client.getUrl(bucketName, filePath).toString();
                uploadedUrls.add(uploadedUrl);
                log.info("업로드완료 URL :" + uploadedUrl);

            } catch (AmazonS3Exception e) {
                log.error(e.getMessage());
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return uploadedUrls;
    }

    //메서드 분리
    private String putObject(ByteArrayOutputStream compressedImageStream, String filePath) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata(); //메타데이터 설정
        metadata.setContentLength(compressedImageStream.size()); //사이즈
        metadata.setContentType("image/jpeg"); //파일 타입

        try (InputStream compressedInputStream = new ByteArrayInputStream(compressedImageStream.toByteArray())) {
            amazonS3Client.putObject(
                    new PutObjectRequest(bucketName, filePath, compressedInputStream, metadata)
                            .withCannedAcl(CannedAccessControlList.PublicReadWrite)
            );
        }
        return amazonS3Client.getUrl(bucketName, filePath).toString();
    }

    private boolean checkContentType(MultipartFile file) {
        if (!Objects.requireNonNull(file.getContentType()).startsWith("image")) {
            log.error("File type not supported " + file.getContentType());
            return true;
        }
        return false;
    }

    private String getFilePath (String folderPath) {
        return folderPath + "/" + UUID.randomUUID().toString();
    }

    public String uploadProfile(MultipartFile file, String folderPath, int size) {
        log.info("-------------파일 업로드 시작-------------");

        //파일 타입 체크
        if (!file.getContentType().startsWith("image")) {
            log.error("File type not supported " + file.getContentType());
        }

        String uploadedUrl = "";

        try (InputStream input = file.getInputStream()) {

            //원본 이미지 압축
            ByteArrayOutputStream compressedImageStream = new ByteArrayOutputStream(); //메모리 내에서 처리

            Thumbnails.of(input)
                    .size(size, size) //이미지 크기
                    .outputFormat("jpg") //압축 형식
                    .toOutputStream(compressedImageStream);

            //파일 이름 및 경로 설정
            String fileName = UUID.randomUUID().toString(); //파일 이름 생성
            String filePath = folderPath + "/" + fileName;

            //클라우드 저장
            ObjectMetadata metadata = new ObjectMetadata(); //메타데이터 설정
            metadata.setContentLength(compressedImageStream.size()); //사이즈
            metadata.setContentType("image/jpeg"); //파일 타입

            try (InputStream compressedInputStream = new ByteArrayInputStream(compressedImageStream.toByteArray())) {
                amazonS3Client.putObject(
                        new PutObjectRequest(bucketName, filePath, compressedInputStream, metadata)
                                .withCannedAcl(CannedAccessControlList.PublicReadWrite)
                );
            }
            // 업로드된 파일 URL 저장
            uploadedUrl = amazonS3Client.getUrl(bucketName, filePath).toString();
            log.info("업로드완료 URL :" + uploadedUrl);

        } catch (AmazonS3Exception e) {
            log.error(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return uploadedUrl;
    }




}
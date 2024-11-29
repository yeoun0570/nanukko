package nanukko.nanukko_back.util;

import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageResizer {

    public static byte[] resizeAndCompressImage(byte[] imageBytes, int targetSize, float quality) throws IOException {
        // 원본 이미지 읽기
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
        BufferedImage originalImage = ImageIO.read(inputStream);

        // 원본 이미지의 비율을 유지하면서 크기 조정
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        int newWidth, newHeight;

        if (originalWidth < originalHeight) {
            newWidth = targetSize;
            newHeight = (int) (((double) originalHeight / originalWidth) * targetSize);
        } else {
            newHeight = targetSize;
            newWidth = (int) (((double) originalWidth / originalHeight) * targetSize);
        }

        // 압축된 이미지를 바이트 배열로 변환
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(originalImage)
                .size(newWidth, newHeight)
                .outputFormat("jpg")
                .outputQuality(quality)
                .toOutputStream(outputStream);

        return outputStream.toByteArray();
    }

}
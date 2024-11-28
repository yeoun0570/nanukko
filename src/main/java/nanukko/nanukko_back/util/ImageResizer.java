package nanukko.nanukko_back.util;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class ImageResizer {

    public static byte[] resizeAndCompressImage(byte[] imageBytes, int targetWidth, int targetHeight, float quality) throws IOException {
        // 원본 이미지 읽기
        ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
        BufferedImage originalImage = ImageIO.read(bais);

        // 원본 이미지의 비율을 유지하면서 크기 조정
        BufferedImage resizedImage = resizeImage(originalImage, targetWidth, targetHeight);

        // 압축된 이미지를 바이트 배열로 변환
        return compressImage(resizedImage, quality);
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        // 원본 이미지 비율 계산
        double ratio = Math.min(
                (double) targetWidth / originalImage.getWidth(),
                (double) targetHeight / originalImage.getHeight()
        );

        // 새로운 크기 계산 (비율 유지)
        int newWidth = (int) (originalImage.getWidth() * ratio);
        int newHeight = (int) (originalImage.getHeight() * ratio);

        // 새로운 이미지 생성
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();

        // 이미지 품질 설정
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 이미지 그리기
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return resizedImage;
    }

    private static byte[] compressImage(BufferedImage image, float quality) throws IOException {
        // 출력 스트림 생성
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // JPEG 이미지 writer 가져오기
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = writers.next();

        // 이미지 출력 스트림 설정
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        writer.setOutput(ios);

        // 압축 품질 설정
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality); // 0.0 (최대 압축) ~ 1.0 (무압축)

        // 이미지 쓰기
        writer.write(null, new IIOImage(image, null, null), param);

        // 리소스 정리
        ios.close();
        writer.dispose();

        return baos.toByteArray();
    }
}
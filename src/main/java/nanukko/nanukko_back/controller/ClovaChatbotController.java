package nanukko.nanukko_back.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.config.ClovaChatbotConfig;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Controller
@Log4j2
@RequiredArgsConstructor
public class ClovaChatbotController {

    private final ClovaChatbotConfig chatbotConfig; // 챗봇 설정 주입

    @MessageMapping("/sendMessage")
    @SendTo("/topic/public")
    public String sendMessage(@Payload String chatMessage) throws IOException {
        // 초기 로깅
        log.info("=== 메시지 처리 시작 ===");
        log.info("받은 메시지: {}", chatMessage);

        // UTF-8 디코딩
        String decodedMessage = URLDecoder.decode(chatMessage, StandardCharsets.UTF_8);
        log.info("디코딩된 메시지: {}", decodedMessage);

        // postback 값 추출 (*T_ 또는 _T_ 제거)
        String messageContent;
        if (decodedMessage.startsWith("*T_") || decodedMessage.startsWith("_T_")) {
            messageContent = decodedMessage.substring(3);
        } else {
            messageContent = decodedMessage;
        }

        // 챗봇 요청 메시지 생성
        String requestMessage = buildRequestMessage(messageContent);
        log.info("챗봇 요청 메시지: {}", requestMessage);

        // 챗봇 API URL 설정
        URL url = new URL(chatbotConfig.getChatbotUrl());

        // API 시그니처 생성
        String signature = makeSignature(requestMessage, chatbotConfig.getSecretKey());

        // HTTP 연결 설정
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json;UTF-8");
        con.setRequestProperty("X-NCP-CHATBOT_SIGNATURE", signature);
        con.setDoOutput(true);

        // 요청 전송
        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.write(requestMessage.getBytes("UTF-8"));
            wr.flush();
        }

        // 응답 처리
        int responseCode = con.getResponseCode();
        log.info("챗봇 응답 코드: {}", responseCode);

        // 응답 읽기 및 반환
        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "UTF-8"))) {
                String response = br.lines().collect(Collectors.joining());
                log.info("챗봇 응답: {}", response);
                return response;
            }
        }

        return con.getResponseMessage(); // 에러 발생시 에러 메시지 반환
    }

    // 챗봇 요청 메시지 생성
    private String buildRequestMessage(String message) {
        JSONObject obj = new JSONObject();
        obj.put("version", "v2");
        obj.put("userId", "U47b00b58c90f8e47428af8b7bddc1231heo2");
        obj.put("timestamp", new Date().getTime());
        if (message == null || message.trim().isEmpty()) {
            obj.put("event", "open");  // 빈 메시지는 open 이벤트로 처리
        } else {
            obj.put("event", "send");
        }

        JSONArray bubbles = new JSONArray();
        JSONObject bubble = new JSONObject();
        bubble.put("type", "text");

        JSONObject data = new JSONObject();
        data.put("description", message);
        bubble.put("data", data);

        bubbles.add(bubble);
        obj.put("bubbles", bubbles);

        return obj.toString();
    }

    // API 시그니처 생성
    private static String makeSignature(String message, String secretKey) {
        try {
            byte[] secretKeyBytes = secretKey.getBytes("UTF-8");
            SecretKeySpec signingKey = new SecretKeySpec(secretKeyBytes, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
            return Base64.encodeBase64String(rawHmac);
        } catch (Exception e) {
            log.error("시그니처 생성 실패", e);
            return "";
        }
    }
}
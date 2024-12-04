package nanukko.nanukko_back.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.config.ClovaChatbotConfig;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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

@Controller
@Log4j2
@RequiredArgsConstructor
public class ClovaChatbotController {

    private final ClovaChatbotConfig chatbotConfig;

    @MessageMapping("/sendMessage") //WebSocket을 통해 /sendMessage로 오는 메시지를 처리
    @SendTo("/topic/public") //처리 결과를 /topic/public으로 브로드캐스트
    public String sendMessage(@Payload String chatMessage) throws IOException {
        // 초기 메시지 로깅
        log.info("=== 메시지 처리 시작 ===");
        // 로그 추가
        log.info("받은 메시지: {}", chatMessage);

        // 직접 UTF-8로 디코딩
        String decodedMessage = URLDecoder.decode(chatMessage, StandardCharsets.UTF_8);
        log.info("디코딩된 메시지: {}", decodedMessage);

        String messageToSend;
        if (decodedMessage.startsWith("*T_") || decodedMessage.startsWith("_T_")) {
            String postback = decodedMessage.substring(3);
            log.info("Postback 값: {}", postback);
            messageToSend = getReqMessage(postback);
        } else {
            messageToSend = getReqMessage(decodedMessage);
        }


        log.info("최종 전송할 메시지: {}", messageToSend);
        log.info("=== 메시지 처리 종료 ===");

        //네이버 클로버 챗봇 API URL 설정
        URL url = new URL(chatbotConfig.getChatbotUrl());

        //사용자 메시지를 API 요청 형식으로 변환
        String message = getReqMessage(chatMessage);
        log.info("API 요청 메시지: {}", message);
        //API 인증을 위한 시그니처 생성
        //시그니처 : 디지털 서명 => 인증된 사용자인지, 요청이 진짜인지, 메시지가 중간에 바뀌었는지 확인
//        String encodeBase64String = makeSignature(message, chatbotConfig.getSecretKey());
        String encodeBase64String = makeSignature(messageToSend, chatbotConfig.getSecretKey());

        //HTTP 연결 설정
        //api서버 접속 (서버 -> 서버 통신)
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST"); // POST 메서드 설정
        con.setRequestProperty("Content-Type", "application/json;UTF-8"); //JSON 형식 지정
        con.setRequestProperty("X-NCP-CHATBOT_SIGNATURE", encodeBase64String); //인증 헤더 추가

        //요청 데이터 전송 설정
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        //메시지 전송
//        wr.write(message.getBytes("UTF-8"));
        wr.write(messageToSend.getBytes("UTF-8"));
        wr.flush();
        wr.close();
        //응답 코드 확인
        int responseCode = con.getResponseCode();

        //응답 처리
        if (responseCode == 200) { // 정상 호출
            //응답 데이터 읽기
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            con.getInputStream(), "UTF-8"));
            String decodedString;
            String jsonString = "";
            while ((decodedString = in.readLine()) != null) {
                jsonString = decodedString;
            }
            // API 응답 후
            log.info("API 응답 코드: {}", responseCode);
            log.info("API 응답 데이터: {}", jsonString);

            //JSON 응답 파싱
            //받아온 값을 세팅하는 부분
            JSONParser jsonparser = new JSONParser();
            try {
                //JSON 구조 파싱: bubbles -> data -> description
                JSONObject json = (JSONObject) jsonparser.parse(jsonString);
                JSONArray bubblesArray = (JSONArray) json.get("bubbles");
                JSONObject bubbles = (JSONObject) bubblesArray.get(0);

                // 버튼형(Template) 응답 처리 추가
                String type = (String) bubbles.get("type");
                JSONObject responseJson = new JSONObject();


                // Quick Reply V1 처리 (환영 인사)
                if ("template".equals(type)) {
                    JSONObject data = (JSONObject) bubbles.get("data");

                    // 커버 메시지 처리
                    JSONObject cover = (JSONObject) data.get("cover");
                    JSONObject coverData = (JSONObject) cover.get("data");
                    responseJson.put("text", coverData.get("description"));

                    // 버튼 처리
                    JSONObject contentTable = (JSONObject) data.get("contentTable");
                    if (contentTable != null) {
                        JSONArray buttonsData = (JSONArray) contentTable.get("data");
                        if (buttonsData != null) {
                            JSONArray buttons = new JSONArray();
                            for (Object btn : buttonsData) {
                                JSONObject button = new JSONObject();
                                JSONObject btnObj = (JSONObject) btn;
                                button.put("text", btnObj.get("title"));
                                button.put("postback", btnObj.get("data"));
                                buttons.add(button);
                            }
                            responseJson.put("buttons", buttons);
                        }
                    }

                }
                // Quick Reply V2 처리 (나머지 객관식)
                else if ("text".equals(type)) {
                    JSONObject data = (JSONObject) bubbles.get("data");
                    String description = (String) data.get("description");
                    responseJson.put("text", description);

                    // quickButtons 처리 추가
                    JSONArray quickButtons = (JSONArray) json.get("quickButtons");
                    if (quickButtons != null) {
                        JSONArray buttons = new JSONArray();

                        for (Object btn : quickButtons) {
                            JSONObject button = new JSONObject();
                            JSONObject btnObj = (JSONObject) btn;
                            button.put("text", btnObj.get("title"));

                            // postback 데이터 추출 - postbackFull 값을 사용
                            JSONObject btnData = (JSONObject) ((JSONObject) btnObj.get("data")).get("action");
                            JSONObject postbackData = (JSONObject) ((JSONObject) btnData).get("data");
                            button.put("postback", postbackData.get("postbackFull")); // postbackFull 사용

                            buttons.add(button);
                        }
                        responseJson.put("buttons", buttons);
                    }
                }
                // 일반 텍스트 응답 처리
                else {
                    JSONObject data = (JSONObject) bubbles.get("data");
                    String description = (String) data.get("description");
                    responseJson.put("text", description);
                }

                // 파싱 후
                log.info("최종 응답 메시지: {}", responseJson.toJSONString());

                return responseJson.toJSONString();

            } catch (Exception e) {
                System.out.println("JSON 파싱 에러:");
                e.printStackTrace();
                return "응답 처리 중 오류가 발생했습니다.";
            }

        } else {  // 에러 발생
            chatMessage = con.getResponseMessage(); //에러 메시지 저장
            return con.getResponseMessage();
        }
    }

    //API 인증을 위한 시그니처 생성 메서드
    //보낼 메세지를 네이버에서 제공해준 암호화로 변경해주는 메소드
    public static String makeSignature(String message, String secretKey) {

        String encodeBase64String = "";

        try {
            //시크릿키를 바이트 배열로 변환
            byte[] secrete_key_bytes = secretKey.getBytes("UTF-8");

            // HMAC-SHA256 암호화 설정
            SecretKeySpec signingKey = new SecretKeySpec(secrete_key_bytes, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            // 메시지 암호화 및 Base64 인코딩
            byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
            encodeBase64String = Base64.encodeBase64String(rawHmac);

            return encodeBase64String;

        } catch (Exception e) {
            System.out.println(e);
        }

        return encodeBase64String;

    }

    //사용자 메시지를 네이버 챗봇 API 형식으로 변환하는 메소드
    //보낼 메세지를 네이버 챗봇에 포맷으로 변경해주는 메소드
    public static String getReqMessage(String voiceMessage) {
        String requestBody = "";

        try {
            JSONObject obj = new JSONObject();
            long timestamp = new Date().getTime();

            log.info("처리할 메시지: {}", voiceMessage);  // 로깅 추가

            System.out.println("##" + timestamp);

            obj.put("version", "v2");
            obj.put("userId", "U47b00b58c90f8e47428af8b7bddc1231heo2");
            obj.put("timestamp", timestamp);

            // postback 메시지 체크
            if (voiceMessage != null && (voiceMessage.startsWith("*T_") || voiceMessage.startsWith("_T_"))) {
                obj.put("event", "send");

                String postback = voiceMessage.substring(3);
                log.info("Postback 값: {}", postback);

                // UnexpiredForm으로 시작하는 postback 값 특별 처리
                if (postback.startsWith("UnexpiredForm")) {
                    String[] parts = postback.split("␞");
                    // parts[2]의 값에 따라 적절한 텍스트로 매핑
                    String mappedText = switch (parts[2]) {
                        case "0" -> "운영 정책";
                        case "1" -> "이용 약관";
                        case "2" -> "개인 정보 처리 방침";
                        case "3" -> "청소년 보호 정책";
                        default -> postback;
                    };
                    postback = mappedText;
                }

                JSONObject bubbles_obj = new JSONObject();
                bubbles_obj.put("type", "text");

                JSONObject data_obj = new JSONObject();
                data_obj.put("description", postback);  // 매핑된 값 사용

                // postback 데이터 설정
                JSONObject action_obj = new JSONObject();
                action_obj.put("type", "postback");

                JSONObject postback_data = new JSONObject();
                postback_data.put("postbackFull", voiceMessage);  // 전체 postback 값 보존
                postback_data.put("postback", postback);
                action_obj.put("data", postback_data);

                bubbles_obj.put("data", data_obj);
                bubbles_obj.put("action", action_obj);

                JSONArray bubbles_array = new JSONArray();
                bubbles_array.add(bubbles_obj);

                obj.put("bubbles", bubbles_array);
            }
            // welcome 이벤트 처리
            else if (voiceMessage == null || voiceMessage.trim().isEmpty()) {
                obj.put("event", "open");
                JSONArray bubbles_array = new JSONArray();
                obj.put("bubbles", bubbles_array);
            }
            // 일반 메시지 처리
            else {
                obj.put("event", "send");

                JSONObject bubbles_obj = new JSONObject();
                bubbles_obj.put("type", "text");

                JSONObject data_obj = new JSONObject();
                data_obj.put("description", voiceMessage);

                bubbles_obj.put("data", data_obj);

                JSONArray bubbles_array = new JSONArray();
                bubbles_array.add(bubbles_obj);

                obj.put("bubbles", bubbles_array);
            }

            requestBody = obj.toString();
            log.info("요청 본문: {}", requestBody);  // 로깅 추가
            return requestBody;

        } catch (Exception e) {
            System.out.println("## Exception : " + e);
            return null;
        }
    }

}

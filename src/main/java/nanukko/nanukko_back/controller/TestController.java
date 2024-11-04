package nanukko.nanukko_back.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    public String getTest() {
        log.info("제발 보내져라..........................");
        return "백엔드에서 데이터 보내기 테스트";
    }
}

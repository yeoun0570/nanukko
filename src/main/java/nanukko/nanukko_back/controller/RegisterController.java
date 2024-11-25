package nanukko.nanukko_back.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.dto.user.UserInfoDTO;
import nanukko.nanukko_back.service.RegisterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/register")
public class RegisterController {

    private final RegisterService registerService;

    @PostMapping("/")
    public ResponseEntity<String> resister(@RequestBody UserInfoDTO dto){
        try{
            registerService.registerUser(dto);
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        }catch (Exception e){
            // 실패 시 예외는 GlobalExceptionHandler에서 처리됨
            throw e; // 예외를 다시 던져서 GlobalExceptionhandler로 위임
        }
    }

    @PostMapping("/duplicatedIdCheck")
    public ResponseEntity<Boolean> duplicatedIdCheck(@RequestBody Map<String, String> request){
        String inputId = request.get("userId");
        boolean exist = registerService.existsById(inputId);

        return ResponseEntity.ok(exist);
    }

}

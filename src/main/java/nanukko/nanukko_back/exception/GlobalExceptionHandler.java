package nanukko.nanukko_back.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleBusinessException(Exception e) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(e.getMessage()));// 예외 발생 시 ErrorResponse를 JSON 형식으로 프론트엔드에 반환
    }
}
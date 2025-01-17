package iuh.fit.backend.identity.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999,"Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001,"Khóa tin nhắn không hợp lệ", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002,"Người dùng đã tồn tại", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003,"username có ít nhất {min}  ký tự", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1004,"password có ít nhất {min}  ký tự", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005,"Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006,"Không xác thực", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007,"Không có quyền", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008,"'Your age must be at least {min}", HttpStatus.BAD_REQUEST),

    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;

}

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


    EVENT_NOT_FOUND(2001, "Sự kiện không tồn tại", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(2002, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    INVALID_EVENT_NAME(2003, "Tên sự kiện có ít nhất {min} ký tự", HttpStatus.BAD_REQUEST),
    INVALID_EVENT_PURPOSE(2004, "Mục đích sự kiện có ít nhất {min} ký tự", HttpStatus.BAD_REQUEST),
    INVALID_EVENT_CONTENT(2005, "Nội dung sự kiện có ít nhất {min} ký tự", HttpStatus.BAD_REQUEST),

    //Mã lỗi cho OrganizerRole
    ROLE_ALREADY_EXISTS(3001, "vai trò của người tổ chức đã tồn tại", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(3002, "vai trò của người tổ chức không tồn tại", HttpStatus.NOT_FOUND),

    // Mã lỗi cho Position
    POSITION_ALREADY_EXISTS(3003, "chức vụ đã tồn tại", HttpStatus.BAD_REQUEST),
    POSITION_NOT_FOUND(3004, "chức vụ không tồn tại", HttpStatus.NOT_FOUND),
    ATTENDEE_NOT_FOUND(3005, "người tham gia không tồn tại", HttpStatus.NOT_FOUND);
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

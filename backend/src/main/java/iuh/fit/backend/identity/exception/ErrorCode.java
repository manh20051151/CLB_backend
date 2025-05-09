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
    USER_NOT_LOCKED(1008,"Tài khoản không bị khóa", HttpStatus.BAD_REQUEST),


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
    ATTENDEE_NOT_FOUND(3005, "người tham gia không tồn tại", HttpStatus.NOT_FOUND),
    GROUP_CHAT_NOT_FOUND(3006, "GROUP không tồn tại", HttpStatus.NOT_FOUND),
    MESSAGE_NOT_FOUND(3007, "MESSAGE không tồn tại", HttpStatus.NOT_FOUND),
    PERMISSION_DENIED(3008, "userId không hợp lệ", HttpStatus.NOT_FOUND),
    USER_NOT_IN_GROUP(3009, "user không có trong group ", HttpStatus.NOT_FOUND),
    INVALID_OPERATION(3010, "Không cho xóa chính leader", HttpStatus.NOT_FOUND),
    LEADER_CANNOT_LEAVE(3011, "leader không được rời nhóm", HttpStatus.NOT_FOUND),
    PASSWORD_OLD_REQUIRED(3012, "Password cũ bắt buộc khi đổi mật khẩu", HttpStatus.NOT_FOUND),
    PASSWORD_OLD_NOT_MATCH(3013, "Password cũ không đúng", HttpStatus.NOT_FOUND),
    EVENT_NOT_DELETED(3014, "Sự kiện chưa bị xóa", HttpStatus.NOT_FOUND),
    EVENT_NOT_IN_PROGRESS(3015, "Sự kiện chưa bắt đầu hoặc đã kết thúc", HttpStatus.NOT_FOUND),
    ATTENDEE_NOT_REGISTERED(3016, "Người dùng chưa đăng ký tham gia sự kiện", HttpStatus.NOT_FOUND),
    ATTENDEE_ALREADY_CHECKED_IN(3017, "Người dùng đã được điểm danh trước đó", HttpStatus.NOT_FOUND),
    QR_CODE_NOT_GENERATED(3018, "QR_CODE chưa được tạo", HttpStatus.NOT_FOUND),
    QR_CODE_GENERATION_FAILED(3019, "QR_CODE thất bại", HttpStatus.NOT_FOUND),
    INVALID_QR_FORMAT(3020, "Định dạng QR code không hợp lệ", HttpStatus.NOT_FOUND),
    EVENT_ATTENDEE_LIMIT_REACHED(3021, "Sự kiện đã đạt số lượng người tham gia tối đa", HttpStatus.NOT_FOUND),


    FILE_NOT_FOUND(3014, "Không tìm thấy file hoặc tin nhắn không chứa file", HttpStatus.NOT_FOUND),
    FILE_DOWNLOAD_ERROR(3015, "Lỗi khi tải file", HttpStatus.NOT_FOUND),
    NEWS_NOT_FOUND(4001, "Lỗi không tìm thấy tin tức", HttpStatus.NOT_FOUND),
    NEWS_NOT_DELETED(4002, "Lỗi Tin tức chưa bị xóa", HttpStatus.NOT_FOUND),

    //NOTIFICATION
    NOTIFICATION_NOT_FOUND(4002, "Lỗi không tìm thấy thông báo", HttpStatus.NOT_FOUND),
    EMAIL_NOT_MATCH(4003, "Email không khớp với tài khoản", HttpStatus.NOT_FOUND),
    INVALID_DATA(4004, "Data không hợp lệ", HttpStatus.NOT_FOUND),
    EMAIL_SENDING_FAILED(4005, "Gửi email thất bại", HttpStatus.NOT_FOUND),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

}

package iuh.fit.backend.identity.controller;

import com.google.zxing.WriterException;
import iuh.fit.backend.Event.service.CloudinaryService;
import iuh.fit.backend.Event.service.QrCodeService;
import iuh.fit.backend.identity.dto.request.ApiResponse;
import iuh.fit.backend.identity.dto.request.UserCreateRequest;
import iuh.fit.backend.identity.dto.request.UserUpdateByUserRequest;
import iuh.fit.backend.identity.dto.request.UserUpdateRequest;
import iuh.fit.backend.identity.dto.response.UserResponse;
import iuh.fit.backend.identity.entity.User;
import iuh.fit.backend.identity.exception.AppException;
import iuh.fit.backend.identity.exception.ErrorCode;
import iuh.fit.backend.identity.repository.UserRepository;
import iuh.fit.backend.identity.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;
    CloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    QrCodeService qrCodeService;
    @PostMapping
    ApiResponse<User> createUser(@RequestBody @Valid UserCreateRequest request) throws IOException, WriterException {
        ApiResponse<User> apiResponse= new ApiResponse<>();

        apiResponse.setResult(userService.createUser(request));
        return apiResponse;
    }

    @GetMapping
    ApiResponse<List<UserResponse>> getUsers(){
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUsers())
                .build();
    }

    @GetMapping("/with-position-and-role")
    public ApiResponse<List<UserResponse>> getUsersWithPositionAndOrganizerRole() {
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUsersWithPositionAndOrganizerRole())
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable String userId){
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUser(userId))
                .build();
    }
    @GetMapping("/notoken/{userId}")
    ApiResponse<UserResponse> getUserNoToken(@PathVariable String userId){
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserNotoken(userId))
                .build();
    }
    @GetMapping("/myInfo")
    ApiResponse<UserResponse> getMyInfo(){
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }


    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable String userId, @RequestBody UserUpdateRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId,request))
                .build();
    }

    @PutMapping("/byuser/{userId}")
    ApiResponse<UserResponse> updateUserByUser(@PathVariable String userId, @RequestBody UserUpdateByUserRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUserByUser(userId,request))
                .build();
    }

    @PatchMapping("/{userId}/avatar")
    public ApiResponse<UserResponse> updateAvatar(
            @PathVariable String userId,
            @RequestPart("file") MultipartFile file) throws IOException {

        String avatarUrl = cloudinaryService.uploadFile(file);
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUserAvatar(userId, avatarUrl))
                .build();
    }

    @DeleteMapping("/{userId}")
    ApiResponse<String> deleteUser(@PathVariable String userId){
        userService.deleteUser(userId);
        return ApiResponse.<String>builder()
                .result("User hs been deleted")
                .build();
    }

    @PutMapping("/{userId}/position")
//    @PreAuthorize("hasRole('ADMIN')") // Chỉ admin có thể thay đổi position
    public ApiResponse<UserResponse> updateUserPosition(
            @PathVariable String userId,
            @RequestParam(required = false) String positionId) {

        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUserPosition(userId, positionId))
                .build();
    }

    @PutMapping("/{userId}/organizer-role")
//    @PreAuthorize("hasRole('ADMIN')") // Chỉ admin có thể thay đổi organizer role
    public ApiResponse<UserResponse> updateUserOrganizerRole(
            @PathVariable String userId,
            @RequestParam(required = false) String organizerRoleId) {

        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUserOrganizerRole(userId, organizerRoleId))
                .build();
    }

    @PostMapping("/{userId}/lock")
//    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> lockUser(
            @PathVariable String userId,
            @RequestParam String lockedById,
            @RequestParam String reason) {

        userService.lockUser(userId, lockedById, reason);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Đã khóa tài khoản thành công")
                .build();
    }

    @PostMapping("/{userId}/unlock")
//    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> unlockUser(@PathVariable String userId) {
        userService.unlockUser(userId);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Đã mở khóa tài khoản thành công")
                .build();
    }

    @GetMapping("/locked")
//    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<UserResponse>> getLockedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.<Page<UserResponse>>builder()
                .code(1000)
                .message("Lấy danh sách tài khoản bị khóa thành công")
                .result(userService.getLockedUsers(pageable))
                .build();
    }

    @GetMapping("/{userId}/qr-code")
    public ApiResponse<String> getUserQrCode(@PathVariable String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getQrCodeUrl() == null) {
            throw new AppException(ErrorCode.QR_CODE_NOT_GENERATED);
        }

        return ApiResponse.<String>builder()
                .code(1000)
                .message("Lấy QR code thành công")
                .result(user.getQrCodeUrl())
                .build();
    }

    // Hoặc nếu muốn trả về ảnh trực tiếp
    @GetMapping("/{userId}/qr-code-image")
    public ResponseEntity<byte[]> getQrCodeImage(@PathVariable String userId) throws IOException, InterruptedException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getQrCodeUrl() == null) {
            throw new AppException(ErrorCode.QR_CODE_NOT_GENERATED);
        }

        byte[] imageBytes = downloadQrCodeImage(user.getQrCodeUrl());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"qr-code.png\"")
                .body(imageBytes);
    }
    private byte[] downloadQrCodeImage(String qrCodeUrl) throws IOException, InterruptedException {
        // Sử dụng Java HttpClient để tải ảnh từ URL
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(qrCodeUrl))
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200) {
            throw new IOException("Không thể tải QR code từ URL: " + qrCodeUrl);
        }

        return response.body();
    }

    @PostMapping("/{userId}/regenerate-qrcode")
    public ApiResponse<String> regenerateQrCode(@PathVariable String userId) {
        try {
            String newQrCodeUrl = qrCodeService.regenerateQrCode(userId);
            return ApiResponse.<String>builder()
                    .code(1000)
                    .message("Tạo lại QR code thành công")
                    .result(newQrCodeUrl)
                    .build();
        } catch (Exception e) {
            throw new AppException(ErrorCode.QR_CODE_GENERATION_FAILED);
        }
    }
}

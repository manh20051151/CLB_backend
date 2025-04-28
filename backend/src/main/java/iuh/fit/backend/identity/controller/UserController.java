package iuh.fit.backend.identity.controller;

import iuh.fit.backend.Event.service.CloudinaryService;
import iuh.fit.backend.identity.dto.request.ApiResponse;
import iuh.fit.backend.identity.dto.request.UserCreateRequest;
import iuh.fit.backend.identity.dto.request.UserUpdateByUserRequest;
import iuh.fit.backend.identity.dto.request.UserUpdateRequest;
import iuh.fit.backend.identity.dto.response.UserResponse;
import iuh.fit.backend.identity.entity.User;
import iuh.fit.backend.identity.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;
    CloudinaryService cloudinaryService;
    @PostMapping
    ApiResponse<User> createUser(@RequestBody @Valid UserCreateRequest request){
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
}

package iuh.fit.backend.identity.service;

import iuh.fit.backend.identity.dto.request.UserCreateRequest;
import iuh.fit.backend.identity.dto.request.UserUpdateByUserRequest;
import iuh.fit.backend.identity.dto.request.UserUpdateRequest;
import iuh.fit.backend.identity.dto.response.UserResponse;
import iuh.fit.backend.identity.entity.Role;
import iuh.fit.backend.identity.entity.User;
//import iuh.fit.backend.identity.enums.Role;
import iuh.fit.backend.identity.exception.AppException;
import iuh.fit.backend.identity.exception.ErrorCode;
import iuh.fit.backend.identity.mapper.UserMapper;
import iuh.fit.backend.identity.repository.RoleRepository;
import iuh.fit.backend.identity.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    // Thêm constant cho avatar mặc định
    private static final String DEFAULT_AVATAR_URL =
            "https://res.cloudinary.com/dnvtmbmne/image/upload/v1744707484/et5vc9r9fejjgrjsvxyn.jpg";
    public User createUser(UserCreateRequest request){

        if(userRepository.existsByUsername(request.getUsername())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Set avatar mặc định
        user.setAvatar(DEFAULT_AVATAR_URL);

//        HashSet<String> roles = new HashSet<>();
//        roles.add(Role.USER.name());
//        user.setRoles(roles);

        Role roleUser = roleRepository.findByName("GUEST")
                .orElseThrow(() -> new RuntimeException("Role USER not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(roleUser);

        user.setRoles(roles);



        return userRepository.save(user);
    }

//    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers(){
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse).toList();
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getUser(String id){
        return userMapper.toUserResponse(userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found")));
    }

//    public UserResponse updateUser(String userId, UserUpdateRequest request){
//        User user = userRepository.findById(userId).orElseThrow(() ->  new AppException(ErrorCode.USER_NOT_EXISTED));
//
//        userMapper.updateUser(user, request);
//
//    //    user.setPassword((passwordEncoder.encode(request.getPassword())));
//    //    var roles = roleRepository.findAllById(request.getRoles());
//    //    user.setRoles(new HashSet<>(roles));
//
//        // Chỉ cập nhật các trường được cung cấp
//        if (request.getPassword() != null) {
//            user.setPassword(passwordEncoder.encode(request.getPassword()));
//        }
//        if (request.getStudentCode() != null) {
//            user.setStudentCode(request.getStudentCode());
//        }
//        if (request.getFirstName() != null) {
//            user.setFirstName(request.getFirstName());
//        }
//        if (request.getLastName() != null) {
//            user.setLastName(request.getLastName());
//        }
//        if (request.getDob() != null) {
//            user.setDob(request.getDob());
//        }
//        if (request.getRoles() != null) {
//            var roles = roleRepository.findAllById(request.getRoles());
//            user.setRoles(new HashSet<>(roles));
//        }
//        if (request.getAvatar() != null) {
//            user.setAvatar(request.getAvatar());
//        }
//        if (request.getEmail() != null) {
//            user.setEmail(request.getEmail());
//        }
//        if (request.getGender() != null) {
//            user.setGender(request.getGender());
//        }
//
//        return userMapper.toUserResponse(userRepository.save(user));
//    }

    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Sử dụng các phương thức setter có điều kiện
        setIfNotNull(request.getPassword(), pwd ->
                user.setPassword(passwordEncoder.encode(pwd)));
        setIfNotNull(request.getFirstName(), user::setFirstName);
        setIfNotNull(request.getLastName(), user::setLastName);
        setIfNotNull(request.getDob(), user::setDob);
        setIfNotNull(request.getAvatar(), user::setAvatar);
        setIfNotNull(request.getEmail(), user::setEmail);
        setIfNotNull(request.getGender(), user::setGender);

        // Xử lý roles
        if (request.getRoles() != null) {
            Set<Role> roles = request.getRoles().stream()
                    .filter(Objects::nonNull)
                    .map(roleRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        return userMapper.toUserResponse(userRepository.save(user));
    }

    // Helper method
    private <T> void setIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    public UserResponse updateUserByUser(String userId, UserUpdateByUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Kiểm tra nếu có yêu cầu đổi mật khẩu
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            if (request.getPasswordOld() == null || request.getPasswordOld().isEmpty()) {
                throw new AppException(ErrorCode.PASSWORD_OLD_REQUIRED);
            }

            if (!passwordEncoder.matches(request.getPasswordOld(), user.getPassword())) {
                throw new AppException(ErrorCode.PASSWORD_OLD_NOT_MATCH);
            }

            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }


        Optional.ofNullable(request.getFirstName()).ifPresent(user::setFirstName);
        Optional.ofNullable(request.getLastName()).ifPresent(user::setLastName);
        Optional.ofNullable(request.getDob()).ifPresent(user::setDob);
        Optional.ofNullable(request.getEmail()).ifPresent(user::setEmail);
        Optional.ofNullable(request.getGender()).ifPresent(user::setGender);

        // Gọi mapper để xử lý các trường đặc biệt (nếu cần)
//        userMapper.updateUserByUser(user, request);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse updateUserAvatar(String userId, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setAvatar(avatarUrl);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void deleteUser(String userId){
        userRepository.deleteById(userId);
    }

    public UserResponse getMyInfo(){
        var context =  SecurityContextHolder.getContext();
        String name =  context.getAuthentication().getName();

         User user =  userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

         return userMapper.toUserResponse(user);
    }

}

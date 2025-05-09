package iuh.fit.backend.identity.service;

import com.google.zxing.WriterException;
import iuh.fit.backend.Event.Entity.OrganizerRole;
import iuh.fit.backend.Event.Entity.Position;
import iuh.fit.backend.Event.repository.OrganizerRoleRepository;
import iuh.fit.backend.Event.repository.PositionRepository;
import iuh.fit.backend.Event.service.QrCodeService;
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
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    private final OrganizerRoleRepository organizerRoleRepository;
    private final PositionRepository positionRepository;

    private final JavaMailSender mailSender;
//    private static final String EMAIL_SUBJECT = "Mật khẩu mới của bạn";
//    private static final String EMAIL_CONTENT = "Mật khẩu mới của bạn là: %s";

    private static final String EMAIL_SUBJECT = "Khôi phục mật khẩu";
    private static final String EMAIL_CONTENT = """
        <p>Xin chào,</p>
        <p>Bạn đã yêu cầu khôi phục mật khẩu. Dưới đây là mật khẩu mới của bạn:</p>
        <p><strong>%s</strong></p>
        <p>Vui lòng đăng nhập và thay đổi mật khẩu ngay sau khi nhận được email này.</p>
        <p>Trân trọng,</p>
        <p>Hệ thống</p>
        """;

    // Thay đổi từ constructor parameters sang field injection
//    @Value("${spring.mail.username}")
//    private String fromEmail;
//
//    @Value("${email.subject.forgot-password}")
//    private String emailSubject;
//
//    @Value("${email.content.forgot-password}")
//    private String emailContent;
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    QrCodeService qrCodeService;
    // Thêm constant cho avatar mặc định
    private static final String DEFAULT_AVATAR_URL =
            "https://res.cloudinary.com/dnvtmbmne/image/upload/v1744707484/et5vc9r9fejjgrjsvxyn.jpg";
    public User createUser(UserCreateRequest request) throws IOException, WriterException {

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

        User userResponse =   userRepository.save(user);
        // Tạo QR code
        String qrCodeUrl = qrCodeService.generateAndSaveQrCode(user.getId());
        user.setQrCodeUrl(qrCodeUrl);
        userResponse =   userRepository.save(user);
        return userResponse;
    }


//    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers(){
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse).toList();
    }

    public List<UserResponse> getUsersWithPositionAndOrganizerRole() {
        return userRepository.findUsersWithPositionAndOrganizerRole().stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getUser(String id){
        return userMapper.toUserResponse(userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found")));
    }

//    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getUserNotoken(String id){
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
            // Kiểm tra nếu roles có chứa USER và joinedDate chưa được thiết lập
            boolean hasUserRole = roles.stream()
                    .anyMatch(role -> "USER".equalsIgnoreCase(role.getName()));

            if (hasUserRole && user.getJoinedDate() == null) {
                user.setJoinedDate(new Date());
            }
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


    @Transactional
    public UserResponse updateUserPosition(String userId, String positionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Position position = null;
        if (positionId != null && !positionId.isEmpty()) {
            position = positionRepository.findById(positionId)
                    .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
        }

        user.setPosition(position);
        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }
    @Transactional
    public UserResponse updateUserOrganizerRole(String userId, String organizerRoleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        OrganizerRole organizerRole = null;
        if (organizerRoleId != null && !organizerRoleId.isEmpty()) {
            organizerRole = organizerRoleRepository.findById(organizerRoleId)
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        }

        user.setOrganizerRole(organizerRole);
        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    // Khóa tài khoản
    @Transactional
    public void lockUser(String userId, String lockedById, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User lockedBy = userRepository.findById(lockedById)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.lock(lockedBy, reason);
        userRepository.save(user);
    }

    // Mở khóa tài khoản
    @Transactional
    public void unlockUser(String userId) {
        User user = userRepository.findLockedUserById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_LOCKED));

        user.unlock();
        userRepository.save(user);
    }

    // Lấy danh sách tài khoản bị khóa
    @Transactional(readOnly = true)
    public Page<UserResponse> getLockedUsers(Pageable pageable) {
        return userRepository.findLockedUsers(pageable)
                .map(userMapper::toUserResponse);
    }

    public void resetPassword(String username, String email) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!user.getEmail().equalsIgnoreCase(email)) {
            throw new AppException(ErrorCode.EMAIL_NOT_MATCH);
        }

        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        sendNewPasswordEmail(user.getEmail(), newPassword);
    }

    private String generateRandomPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(8);
        Random random = new Random();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }

    private void sendNewPasswordEmail(String toEmail, String newPassword) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("nguyenvietmanh1409@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject(EMAIL_SUBJECT);
            helper.setText(String.format(EMAIL_CONTENT, newPassword), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email", e);
            throw new AppException(ErrorCode.EMAIL_SENDING_FAILED);
        }
    }
}

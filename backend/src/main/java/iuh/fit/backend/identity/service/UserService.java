package iuh.fit.backend.identity.service;

import iuh.fit.backend.identity.dto.request.UserCreateRequest;
import iuh.fit.backend.identity.dto.request.UserUpdateRequest;
import iuh.fit.backend.identity.dto.response.UserResponse;
import iuh.fit.backend.identity.entity.User;
import iuh.fit.backend.identity.enums.Role;
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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    public User createUser(UserCreateRequest request){

        if(userRepository.existsByUsername(request.getUsername())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<String> roles = new HashSet<>();
        roles.add(Role.USER.name());

//        user.setRoles(roles);

        return userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers(){
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse).toList();
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getUser(String id){
        return userMapper.toUserResponse(userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found")));
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request){
    User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    userMapper.updateUser(user, request);

    user.setPassword((passwordEncoder.encode(request.getPassword())));
    var roles = roleRepository.findAllById(request.getRoles());
    user.setRoles(new HashSet<>(roles));

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

package com.example.storage.service;

import com.example.storage.dto.AuthRequest;
import com.example.storage.dto.AuthResponse;
import com.example.storage.dto.UserResponse;
import com.example.storage.entity.User;
import com.example.storage.enums.Role;
import com.example.storage.mapper.UserMapper;
import com.example.storage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public List<UserResponse> getAllUsers(){
        if(isAdmin()){
            return userRepository.findAll().stream().map(UserMapper::toResponse).toList();
        }
        else {
            throw new RuntimeException("Yetkiniz yok.");
        }
    }

    public AuthResponse register(AuthRequest request){
        if (userRepository.findByUserName(request.getUserName()).isPresent()) {
            throw new RuntimeException("Bu kullanıcı adı zaten kayıtlı!");
        }

        User user = new User();
        user.setUserName(request.getUserName());
        user.setPassword(request.getPassword());

        if (user != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(Role.USER);

            userRepository.save(user);

            var jwtToken = jwtService.generateToken(user);
            return AuthResponse.builder().token(jwtToken).build();
        }

        throw new RuntimeException("Kayıt işlemi sırasında kullanıcı oluşturulamadı.");
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUserName(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByUserName(request.getUserName())
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı."));

        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder().token(jwtToken).build();
    }

}

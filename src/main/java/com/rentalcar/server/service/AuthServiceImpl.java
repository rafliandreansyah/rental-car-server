package com.rentalcar.server.service;

import com.rentalcar.server.entity.User;
import com.rentalcar.server.entity.UserRoleEnum;
import com.rentalcar.server.model.AuthenticateRequest;
import com.rentalcar.server.model.AuthenticateResponse;
import com.rentalcar.server.model.RegisterRequest;
import com.rentalcar.server.repository.UserRepository;
import com.rentalcar.server.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final ValidationService validationService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    @Override
    public String register(RegisterRequest request) {
        validationService.validate(request);

        Optional<User> userEmail = userRepository.findByEmail(request.getEmail());
        if (userEmail.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email already register");
        }

        Optional<User> userByPhoneNumber = userRepository.findByPhoneNumber(request.getPhone().trim());
        if (userByPhoneNumber.isPresent()){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "phone number already use");
        }

        var userData = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhone())
                .name(request.getName())
                .role(UserRoleEnum.USER)
                .build();

        userRepository.save(userData);

        return "registration successful";
    }

    @Transactional
    @Override
    public User createAdmin(User user) {
        var userData = User.builder()
                .email(user.getEmail())
                .password(passwordEncoder.encode(user.getPassword()))
                .phoneNumber(user.getPhoneNumber())
                .name(user.getName())
                .role(UserRoleEnum.ADMIN)
                .build();
        return userRepository.save(userData);
    }

    @Override
    public AuthenticateResponse authenticate(AuthenticateRequest authenticateRequest) {
        validationService.validate(authenticateRequest);

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticateRequest.getEmail(),
                    authenticateRequest.getPassword()
            ));
        }catch (BadCredentialsException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid email or password");
        }


        User user = userRepository.findByEmail(authenticateRequest.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "email doesn't exists"));


        return AuthenticateResponse.builder()
                .token(jwtService.generateToken(user))
                .build();
    }
}
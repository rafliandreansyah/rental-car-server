package com.rentalcar.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentalcar.server.component.TokenGenerator;
import com.rentalcar.server.entity.ResetToken;
import com.rentalcar.server.entity.User;
import com.rentalcar.server.entity.UserRoleEnum;
import com.rentalcar.server.model.*;
import com.rentalcar.server.repository.ResetTokenRepository;
import com.rentalcar.server.repository.UserRepository;
import com.rentalcar.server.security.JwtService;
import com.rentalcar.server.util.DateTimeUtils;
import com.rentalcar.server.util.UUIDUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final ResetTokenRepository resetTokenRepository;
    private final ValidationService validationService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenGenerator tokenGenerator;
    private final DateTimeUtils dateTimeUtils;
    private final UUIDUtils uuidUtils;
    private final TransactionalEmailsApi api;
    private final SendSmtpEmailSender sender;
    private final SendSmtpEmail sendSmtpEmail;

    @Transactional
    @Override
    public String register(RegisterRequest request) {
        validationService.validate(request);

        Optional<User> userEmail = userRepository.findByEmail(request.getEmail());
        if (userEmail.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email already register");
        }

        Optional<User> userByPhoneNumber = userRepository.findByPhoneNumber(request.getPhone().trim());
        if (userByPhoneNumber.isPresent()) {
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
    public User createUser(User user) {
        var userData = User.builder()
                .email(user.getEmail())
                .password(passwordEncoder.encode(user.getPassword()))
                .phoneNumber(user.getPhoneNumber())
                .name(user.getName())
                .role(UserRoleEnum.USER)
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
        } catch (BadCredentialsException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid email or password");
        }


        User user = userRepository.findByEmail(authenticateRequest.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "email doesn't exists"));


        return AuthenticateResponse.builder()
                .token(jwtService.generateToken(user))
                .build();
    }

    @Transactional
    @Override
    public String requestResetPassword(ResetPasswordRequest resetPasswordRequest, HttpServletRequest request) {

        User user = userRepository.findByEmail(resetPasswordRequest.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        Optional<ResetToken> resetTokenData = resetTokenRepository.findByUserId(user.getId());
        resetTokenData.ifPresent(resetToken -> resetTokenRepository.deleteById(resetToken.getId()));

        // Generate token
        String token = tokenGenerator.generateToken();
        LocalDateTime localDateTime = LocalDateTime.now().plusHours(1);

        // Save data to reset token
        ResetToken resetTokenSaved = resetTokenRepository.save(ResetToken.builder()
                .token(token)
                .user(user)
                .expiredDate(dateTimeUtils.instantFromLocalDateTimeZoneJakarta(localDateTime))
                .build());

        // Get base url to access reset token link
        String baseUrl = String.format("%s://%s:%d/api/v1/auth/reset-password?token=%s",
                request.getScheme(),
                request.getServerName(),
                request.getServerPort(),
                resetTokenSaved.getToken());

        log.debug(baseUrl);

        /*
         * Sending link reset password to email
         * */
        try {

            // Get template html
            GetSmtpTemplateOverview responseTemplate = api.getSmtpTemplate(1L);
            System.out.println(responseTemplate.getHtmlContent().replace("<a href=\"#top\"" , "<a href=\""+ baseUrl + "\""));
            String templateResetPasswordEmail = responseTemplate.getHtmlContent().replace("<a href=\"#top\"" , "<a href=\""+ baseUrl + "\"");

            //set to email
            List<SendSmtpEmailTo> toList = new ArrayList<>();
            SendSmtpEmailTo to = new SendSmtpEmailTo();
            to.setEmail(user.getEmail());
            to.setName(user.getName());
            toList.add(to);

            // Send email configure
            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(toList);
            sendSmtpEmail.setHtmlContent(templateResetPasswordEmail);
            sendSmtpEmail.setSubject("Reset Password Request");

            // Send reset password to email
            CreateSmtpEmail response = api.sendTransacEmail(sendSmtpEmail);
            System.out.println(response.toString());

            return "success send link reset password to email";


        }catch (Exception e) {
            System.out.println("Exception occurred:- " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed send reset password link to email");
        }


    }

    @Override
    public ResetToken requestResetPassword(ResetPasswordRequest resetPasswordRequest) {
        User user = userRepository.findByEmail(resetPasswordRequest.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        Optional<ResetToken> resetTokenData = resetTokenRepository.findByUserId(user.getId());
        resetTokenData.ifPresent(resetToken -> resetTokenRepository.deleteById(resetToken.getId()));

        // Generate token
        String token = tokenGenerator.generateToken();
        LocalDateTime localDateTime = LocalDateTime.now().plusHours(1);

        // Save data to reset token
        return resetTokenRepository.save(ResetToken.builder()
                .token(token)
                .user(user)
                .expiredDate(dateTimeUtils.instantFromLocalDateTimeZoneJakarta(localDateTime))
                .build());
    }

    @Override
    public ResetPasswordResponse getResetTokenByToken(String token) {
        ResetToken resetTokenData = resetTokenRepository.findByToken(token).orElseThrow(() -> new ResponseStatusException(HttpStatus.GONE, "link reset password is expired"));

        LocalDateTime expiredDate = dateTimeUtils.localDateTimeFromInstantZoneJakarta(resetTokenData.getExpiredDate());

        if (LocalDateTime.now().isAfter(expiredDate)) {
            throw new ResponseStatusException(HttpStatus.GONE, "link reset password is expired");
        }

        return ResetPasswordResponse.builder()
                .userId(resetTokenData.getUser().getId().toString())
                .build();
    }

    @Transactional
    @Override
    public String resetNewPassword(ResetNewPasswordRequest resetNewPasswordRequest) {

        UUID id = uuidUtils.uuidFromString(resetNewPasswordRequest.getUserId(), "user not found");

        ResetToken resetTokenData = resetTokenRepository.findByUserIdAndToken(id, resetNewPasswordRequest.getToken()).orElseThrow(() -> new ResponseStatusException(HttpStatus.GONE, "reset password is expired"));

        User userData = userRepository.findById(resetTokenData.getUser().getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        userData.setPassword(passwordEncoder.encode(resetNewPasswordRequest.getNewPassword()));

        userRepository.save(userData);
        return "success reset new password";
    }
}

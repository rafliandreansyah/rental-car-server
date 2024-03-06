package com.rentalcar.server.service;

import com.rentalcar.server.entity.ResetToken;
import com.rentalcar.server.entity.User;
import com.rentalcar.server.model.*;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    String register(RegisterRequest request);

    User createAdmin(User user);

    User createUser(User user);

    AuthenticateResponse authenticate(AuthenticateRequest authenticateRequest);

    String requestResetPassword(ResetPasswordRequest resetPasswordRequest, HttpServletRequest request);

    ResetPasswordResponse getResetTokenByToken(String token);

    String resetNewPassword(ResetNewPasswordRequest resetNewPasswordRequest);

}

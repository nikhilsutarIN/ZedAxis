package com.ecom.config;

import com.ecom.model.User;
import com.ecom.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private UserService userService;

    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        String email = request.getParameter("username");

        User user = userService.getUserByEmail(email);

        if(user != null) {
            if (!user.getIsEnabled()) {
                exception = new LockedException("Your account is disabled. Check your email or send request to zedaxisio@gmail.com");
            }
            else {
                exception = new LockedException("Something went wrong");
            }
        } else {
            exception = new LockedException("Email or Password is invalid");
        }

        super.setDefaultFailureUrl("/signin?error");
        super.onAuthenticationFailure(request, response, exception);

    }

}

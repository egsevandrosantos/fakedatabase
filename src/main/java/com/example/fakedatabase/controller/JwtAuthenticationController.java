package com.example.fakedatabase.controller;

import com.example.fakedatabase.config.JwtTokenUtil;
import com.example.fakedatabase.model.JwtRequest;
import com.example.fakedatabase.model.JwtResponse;
import com.example.fakedatabase.model.User;
import com.example.fakedatabase.service.JwtUserDetailsService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class JwtAuthenticationController {
    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest, HttpServletResponse response) throws Exception {
        try {
            final User userDetails = userDetailsService
                .authenticateUser(authenticationRequest.getEmail(), authenticationRequest.getPassword());
            if (userDetails != null ) {
                final String token = jwtTokenUtil.generateToken(userDetails);
                final String refreshToken = jwtTokenUtil.generateRefreshToken();
                User user = new User(userDetails.getId(), userDetails.getEmail());
                Cookie cookie = new Cookie("refreshToken", refreshToken);
                cookie.setMaxAge(1 * 24 * 60 * 60); // expires in 1 day
                cookie.setSecure(true);
                cookie.setHttpOnly(true);
                response.addCookie(cookie);
                return ResponseEntity.ok().header("token", token).body(user);
            }
            return ResponseEntity.status(401).body("Invalid credentials");
        } catch (UsernameNotFoundException ex) {
            return ResponseEntity.status(401).body("Invalid credentials");
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}
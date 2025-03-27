package com.example.express.controller;

import com.example.express.common.ApiResponse;
import com.example.express.config.JwtConfig;
import com.example.express.entity.User;
import com.example.express.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器，处理用户登录、注册等认证相关请求
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        String captcha = loginRequest.get("captcha");
        String captchaKey = loginRequest.get("captchaKey");
        
        // 验证图形验证码
        if (!userService.validateCaptcha(captchaKey, captcha)) {
            return ApiResponse.error("验证码错误或已过期");
        }

        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 获取用户信息
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ApiResponse.error("用户不存在");
            }
            
            if (!user.isEnabled()) {
                return ApiResponse.error("账号已被禁用");
            }
            
            // 更新最后登录时间
            user.setLastLogin(LocalDateTime.now());
            userService.updateUser(user);
            
            // 生成JWT令牌
            String jwt = jwtConfig.generateToken(username, user.getRole());
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", jwt);
            data.put("user", user);
            
            return ApiResponse.success("登录成功", data);
        } catch (Exception e) {
            return ApiResponse.error("用户名或密码错误");
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse register(@RequestBody User user) {
        // 检查用户名是否已存在
        if (userService.existsByUsername(user.getUsername())) {
            return ApiResponse.error("用户名已存在");
        }
        
        // 设置默认角色和状态
        user.setRole("CUSTOMER");
        user.setStatus(1);
        
        // 使用MD5加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 保存用户
        userService.saveUser(user);
        
        return ApiResponse.success("注册成功");
    }

    /**
     * 获取图形验证码
     */
    @GetMapping("/captcha")
    public ApiResponse getCaptcha() {
        Map<String, String> captchaInfo = userService.generateCaptcha();
        return ApiResponse.success("获取验证码成功", captchaInfo);
    }
}
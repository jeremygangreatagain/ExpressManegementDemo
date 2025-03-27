package com.example.express.service.impl;

import com.example.express.entity.User;
import com.example.express.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.express.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final long CAPTCHA_EXPIRATION = 5; // 验证码有效期（分钟）

    @Override
    public User findByUsername(String username) {
        return userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMapper.selectCount(new QueryWrapper<User>().eq("username", username)) > 0;
    }

    @Override
    public void saveUser(User user) {
        userMapper.insert(user);
    }

    @Override
    public void updateUser(User user) {
        userMapper.updateById(user);
    }

    @Override
    public boolean removeById(Long id) {
        return userMapper.deleteById(id) > 0;
    }
    
    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }
    
    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page, 
            QueryWrapper<User> queryWrapper) {
        return super.page(page, queryWrapper);
    }
    
    @Override
    public Map<String, String> generateCaptcha() {
        // 生成随机验证码
        String captchaCode = generateRandomCode(4);
        
        // 生成验证码图片
        String captchaImage = generateCaptchaImage(captchaCode);
        
        // 生成唯一key
        String captchaKey = UUID.randomUUID().toString();
        
        // 将验证码存入Redis，设置过期时间
        redisTemplate.opsForValue().set(CAPTCHA_PREFIX + captchaKey, captchaCode, CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        
        // 返回验证码信息
        Map<String, String> captchaInfo = new HashMap<>();
        captchaInfo.put("captchaKey", captchaKey);
        captchaInfo.put("captchaImage", captchaImage);
        
        return captchaInfo;
    }

    @Override
    public boolean validateCaptcha(String captchaKey, String captchaCode) {
        if (captchaKey == null || captchaCode == null) {
            return false;
        }
        
        // 从Redis获取验证码
        String storedCaptcha = redisTemplate.opsForValue().get(CAPTCHA_PREFIX + captchaKey);
        
        // 验证码校验成功后，删除Redis中的验证码
        if (storedCaptcha != null && storedCaptcha.equalsIgnoreCase(captchaCode)) {
            redisTemplate.delete(CAPTCHA_PREFIX + captchaKey);
            return true;
        }
        
        return false;
    }
    
    /**
     * 生成随机验证码
     */
    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        
        return sb.toString();
    }
    
    /**
     * 生成验证码图片并转为Base64
     */
    private String generateCaptchaImage(String code) {
        int width = 100;
        int height = 40;
        
        // 创建图像缓冲区
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // 设置背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        
        // 设置字体
        g.setFont(new Font("Arial", Font.BOLD, 20));
        
        // 添加干扰线
        Random random = new Random();
        g.setColor(new Color(160, 160, 160));
        for (int i = 0; i < 8; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            g.drawLine(x1, y1, x2, y2);
        }
        
        // 添加噪点
        for (int i = 0; i < 50; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.drawOval(x, y, 1, 1);
        }
        
        // 绘制验证码
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(random.nextInt(100), random.nextInt(100), random.nextInt(100)));
            g.drawString(String.valueOf(code.charAt(i)), 20 * i + 10, 25);
        }
        
        g.dispose();
        
        // 转换为Base64
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }
}
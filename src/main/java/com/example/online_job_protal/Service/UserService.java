package com.example.online_job_protal.Service;

import com.example.online_job_protal.Model.PasswordResetToken;
import com.example.online_job_protal.Model.UserModel;

import com.example.online_job_protal.repository.PasswordResetTokenRepository;
import com.example.online_job_protal.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    private PasswordResetTokenRepository tokenRepository;
    private JavaMailSender mailSender;

    public UserService(UserRepository userRepository, PasswordResetTokenRepository tokenRepository, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
    }


    public UserModel saveUser(UserModel user) {
        return userRepository.save(user);
    }

    public Optional<UserModel> login(String email, String password) {
        Optional<UserModel> user = userRepository.findByEmail(email);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            return user;
        }
        return Optional.empty();
    }

    // Method to retrieve all users with pagination and sorting
    // Method to retrieve all users excluding administrators with pagination and sorting
    public List<UserModel> getAllUsers() {
        return userRepository.findAll();
    }


    // Method to search users by username excluding administrators with pagination and sortin

    // Method to find a user by ID
    public UserModel getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user ID:" + id));
    }

    // Method to delete a user by ID
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    public int getTotalPages(int pageSize) {
        long totalUsers = userRepository.count();
        return (int) Math.ceil((double) totalUsers / pageSize);
    }
    // 1. Create a Password Reset Token
    public String createPasswordResetToken(String email) {
        Optional<UserModel> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(token, user.get(), new Date(System.currentTimeMillis() + 30 * 60 * 1000)); // 30 minutes expiry
            tokenRepository.save(resetToken);
            return token;
        }
        return null;
    }
    // 2. Send Reset Email
    public void sendResetEmail(String email, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("Password Reset Request");
            helper.setText("<p>You requested a password reset. Click the link below to reset your password:</p>" +
                    "<p><a href=\"" + resetLink + "\">Reset Password</a></p>", true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            // Handle exception
        }
    }

    // 3. Validate Token
    public boolean validateToken(String token) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);
        return resetToken.isPresent() && resetToken.get().getExpiryDate().after(new Date());
    }

    // 4. Update Password
    public boolean updatePassword(String token, String password) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);
        if (resetToken.isPresent() && resetToken.get().getExpiryDate().after(new Date())) {
            UserModel user = resetToken.get().getUser();
            user.setPassword(password);
            userRepository.save(user);
            tokenRepository.delete(resetToken.get()); // Delete token after successful password reset
            return true;
        }
        return false;
    }

}

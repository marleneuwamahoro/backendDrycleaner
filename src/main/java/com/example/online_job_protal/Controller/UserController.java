package com.example.online_job_protal.Controller;


import com.example.online_job_protal.Model.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.online_job_protal.Model.UserModel;
import com.example.online_job_protal.Service.RoleService;
import com.example.online_job_protal.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserModel userModel, HttpServletRequest request) {
        // Assuming userService.login() now accepts UserModel
        Optional<UserModel> user = userService.login(userModel.getEmail(), userModel.getPassword());

        if (user.isPresent()) {
            HttpSession session = request.getSession();
            String role = user.get().getRole();

            // Set session attributes
            session.setAttribute("email", user.get().getEmail());
            session.setAttribute("role", user.get().getRole());
            session.setAttribute("firstname", user.get().getFirstName());
            session.setAttribute("lastname", user.get().getLastName());

            // Prepare response data
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("role", role);
            responseData.put("email", user.get().getEmail());
            responseData.put("firstname", user.get().getFirstName());
            responseData.put("lastname", user.get().getLastName());

            // List of allowed menus
            List<String> allowedMenus = roleService.getAllowedMenus(role);
            responseData.put("allowedMenus", allowedMenus);

            // Return a response with status 200 (OK)
            return ResponseEntity.ok(responseData);
        } else {
            // Return error response if login failed
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("errorMessage", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse); // 401 Unauthorized
        }
    }


    //Signup to react
    @PostMapping("/signup")
    public ResponseEntity<UserModel> createUser(@RequestBody UserModel user) {
        UserModel createdUser = userService.saveUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }


    @GetMapping("/users")
    public ResponseEntity<List<UserModel>> getAllUsers() {
        try {
            List<UserModel> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @GetMapping("/UserDashboard")
    public String showUserDashboard(Model model,HttpSession session) {// Debugging output
        String firstname = (String) session.getAttribute("firstname");
        String lastname = (String) session.getAttribute("lastname");
        String role = (String) session.getAttribute("role");

        List<String> allowedMenus = roleService.getAllowedMenus(role); // Use roleService directly

        model.addAttribute("allowedMenus", allowedMenus);


        model.addAttribute("username", role);
        model.addAttribute("firstname", firstname);
        model.addAttribute("lastname", lastname);//
        return "UserDashboard";
    }

    // Update User
    @PostMapping("/edit/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UserModel user) {
        user.setId(id); // Set the ID of the user to update
        try {
            userService.saveUser(user); // Save updated user
            return ResponseEntity.ok("User updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating user");
        }
    }

    // Delete User
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id); // Delete user by ID
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user");
        }
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }

        // Generate reset token for the provided email
        String token = userService.createPasswordResetToken(email);

        if (token != null) {
            // Create a reset link (replace with your React frontend's URL)
            String resetLink = "http://localhost:3000/resetpassword?token=" + token;

            try {
                // Send the reset email
                userService.sendResetEmail(email, resetLink);
                return ResponseEntity.ok("Reset email sent.");
            } catch (Exception e) {
                // Log the exception (you might want to use a logging framework here)
                System.err.println("Error sending reset email: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to send reset email. Please try again later.");
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Email not found.");
    }

    @GetMapping("/verify-reset-token")
    public ResponseEntity<String> verifyResetToken(@RequestParam String token) {
        boolean isValid = userService.validateToken(token);

        if (isValid) {
            return ResponseEntity.ok("Valid token");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Token and new password are required.");
        }

        boolean success = userService.updatePassword(token, newPassword);

        if (success) {
            return ResponseEntity.ok("Password reset successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token.");
        }
    }
}

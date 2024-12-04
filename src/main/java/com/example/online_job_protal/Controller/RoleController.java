package com.example.online_job_protal.Controller;
import com.example.online_job_protal.Model.Role;
import com.example.online_job_protal.Service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public ResponseEntity<String> createRole(@RequestBody Role role) {
    roleService.save(role);
    return ResponseEntity.status(HttpStatus.CREATED).body("Role added successfully.");
}

    @GetMapping
    public ResponseEntity<List<Role>> listRoles() {
        List<Role> roles = roleService.findAll();
        return ResponseEntity.ok(roles);
    }

}


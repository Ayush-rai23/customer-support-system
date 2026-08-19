package com.support.backend.controller;

import com.support.backend.dto.AdminView;
import com.support.backend.dto.TicketMapper;
import com.support.backend.repository.AdminRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only admin listing, used to populate the ticket assignee dropdown. */
@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminRepository adminRepository;

    public AdminController(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @GetMapping
    public ResponseEntity<List<AdminView>> list() {
        List<AdminView> admins = adminRepository.findAll().stream()
                .map(TicketMapper::toAdminView)
                .toList();
        return ResponseEntity.ok(admins);
    }
}

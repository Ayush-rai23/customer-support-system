package com.support.backend.service;

import com.support.backend.repository.AdminRepository;
import com.support.backend.security.AdminPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    public AdminUserDetailsService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return adminRepository.findByEmail(email)
                .map(AdminPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("No admin with email " + email));
    }
}

package com.support.backend.config;

import com.support.backend.entity.Admin;
import com.support.backend.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration(proxyBeanMethods = false)
public class AdminSeeder {

    @Bean
    public CommandLineRunner seedDefaultAdmin(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.default-email}") String defaultEmail,
            @Value("${app.admin.default-password}") String defaultPassword) {
        return args -> {
            if (adminRepository.count() == 0) {
                Admin admin = Admin.builder()
                        .email(defaultEmail)
                        .password(passwordEncoder.encode(defaultPassword))
                        .build();
                adminRepository.save(admin);
            }
        };
    }
}

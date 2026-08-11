package com.support.backend.config;

import com.support.backend.entity.Admin;
import com.support.backend.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration(proxyBeanMethods = false)
public class AdminSeeder {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);
    private static final String DEV_DEFAULT_PASSWORD = "changeme";

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

                if (DEV_DEFAULT_PASSWORD.equals(defaultPassword)) {
                    log.warn("Seeded default admin '{}' with the built-in dev password. "
                            + "Set ADMIN_DEFAULT_PASSWORD (and rotate this account) before any non-dev deployment.",
                            defaultEmail);
                } else {
                    log.info("Seeded default admin '{}'.", defaultEmail);
                }
            }
        };
    }
}

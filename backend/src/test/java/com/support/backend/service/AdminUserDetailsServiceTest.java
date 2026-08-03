package com.support.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.support.backend.entity.Admin;
import com.support.backend.repository.AdminRepository;
import com.support.backend.security.AdminPrincipal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class AdminUserDetailsServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Test
    void loadUserByUsername_returnsAdminPrincipal_whenAdminExists() {
        Admin admin = Admin.builder().id(1L).email("admin@support.local").password("hashed").build();
        when(adminRepository.findByEmail("admin@support.local")).thenReturn(Optional.of(admin));

        AdminUserDetailsService service = new AdminUserDetailsService(adminRepository);
        UserDetails result = service.loadUserByUsername("admin@support.local");

        assertThat(result).isInstanceOf(AdminPrincipal.class);
        assertThat(result.getUsername()).isEqualTo("admin@support.local");
        assertThat(((AdminPrincipal) result).getAdmin().getEmail()).isEqualTo("admin@support.local");
    }

    @Test
    void loadUserByUsername_throwsUsernameNotFoundException_whenAdminNotFound() {
        when(adminRepository.findByEmail("missing@support.local")).thenReturn(Optional.empty());

        AdminUserDetailsService service = new AdminUserDetailsService(adminRepository);

        assertThatThrownBy(() -> service.loadUserByUsername("missing@support.local"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}

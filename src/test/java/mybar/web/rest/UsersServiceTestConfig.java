package mybar.web.rest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Arrays;

@TestConfiguration
public class UsersServiceTestConfig {

    private static UserDetails buildActive(User.UserBuilder userBuilder) {
        return userBuilder
                .accountExpired(false)
                .accountLocked(false)
                .disabled(false)
                .credentialsExpired(false)
                .build();
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {

        User.UserBuilder testUserBuilder = User
                .withUsername("user")
                .password("user")
                .roles("USER");
        UserDetails testUser = buildActive(testUserBuilder);

        User.UserBuilder adminUserBuilder = User
                .withUsername("admin")
                .password("admin")
                .roles("ADMIN");
        UserDetails adminUser = buildActive(adminUserBuilder);

        User.UserBuilder superUserBuilder = User
                .withUsername("super")
                .password("super")
                .roles("SUPER");
        UserDetails superUser = buildActive(superUserBuilder);

        return new InMemoryUserDetailsManager(Arrays.asList(testUser, adminUser, superUser));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PlainTextPasswordEncoder();
    }

    private static class PlainTextPasswordEncoder implements PasswordEncoder {

        @Override
        public String encode(CharSequence charSequence) {
            return charSequence.toString();
        }

        @Override
        public boolean matches(CharSequence charSequence, String s) {
            return charSequence.toString().equals(s);
        }
    }

}

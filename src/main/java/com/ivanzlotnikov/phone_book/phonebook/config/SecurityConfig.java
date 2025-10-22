
package com.ivanzlotnikov.phone_book.phonebook.config;

import com.ivanzlotnikov.phone_book.phonebook.auth.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
//            .csrf(AbstractHttpConfigurer::disable)
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
            .authorizeHttpRequests(authz -> authz
                // 1. Публичные ресурсы
                .requestMatchers("/", "/home", "/css/**", "/js/**", "/images/**",
                    "/webjars/**", "/login", "/error").permitAll()
                // 2. Ресурсы только для АДМИНА
                .requestMatchers("/contacts/new", "/contacts/save", "/contacts/edit/**", "/contacts/delete/**").hasRole("ADMIN")
                // 3. Ресурсы для всех аутентифицированных пользователей
                .requestMatchers("/contacts", "/contacts/**").authenticated()
                // 4. Все остальные запросы требуют аутентификации
                .anyRequest().authenticated()
            )
            .userDetailsService(userDetailsService)
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/contacts", true) // Перенаправлять на контакты после логина
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );

        return http.build();
    }
}


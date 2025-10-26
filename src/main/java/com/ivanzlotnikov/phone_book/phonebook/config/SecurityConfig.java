package com.ivanzlotnikov.phone_book.phonebook.config;

import com.ivanzlotnikov.phone_book.phonebook.auth.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Конфигурация Spring Security.
 * Настраивает аутентификацию, авторизацию и защиту от CSRF-атак.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String[] PUBLIC_PATHS = {
        "/", "/home", "/css/**", "/js/**", "/images/**",
        "/webjars/**", "/login", "/error"
    };
    private static final String[] ADMIN_PATHS = {
        "/contacts/new", "/contacts/save", "/contacts/edit/**",
        "/contacts/delete/**"
    };
    private static final String[] AUTH_PATHS = {
        "/contacts", "/contacts/**"
    };

    /**
     * Создает кодировщик паролей BCrypt.
     *
     * @return экземпляр BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Настраивает цепочку фильтров безопасности.
     *
     * @param http объект конфигурации HTTP безопасности
     * @return сконфигурированная цепочка фильтров безопасности
     * @throws Exception в случае ошибки конфигурации
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(this::configureCsrf)
            .authorizeHttpRequests(this::configureAuthorization)
            .userDetailsService(userDetailsService)
            .formLogin(this::configureFormLogin)
            .logout(this::configureLogout);
        return http.build();
    }

    /**
     * Настраивает защиту от CSRF-атак.
     *
     * @param csrf конфигуратор CSRF
     */
    private void configureCsrf(CsrfConfigurer<HttpSecurity> csrf) {
        csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
    }

    /**
     * Настраивает правила авторизации для различных URL.
     *
     * @param authz менеджер авторизации запросов
     */
    private void configureAuthorization(
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz) {
        authz
            .requestMatchers(PUBLIC_PATHS).permitAll()
            .requestMatchers(ADMIN_PATHS).hasRole(ROLE_ADMIN)
            .requestMatchers(AUTH_PATHS).authenticated()
            .anyRequest().authenticated();
    }

    /**
     * Настраивает форму входа в систему.
     *
     * @param form конфигуратор формы входа
     */
    private void configureFormLogin(FormLoginConfigurer<HttpSecurity> form) {
        form
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .defaultSuccessUrl("/contacts", true)
            .failureUrl("/login?error=true")
            .permitAll();
    }

    /**
     * Настраивает выход из системы.
     *
     * @param logout конфигуратор выхода
     */
    private void configureLogout(LogoutConfigurer<HttpSecurity> logout) {
        logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout=true")
            .permitAll();
    }
}


package com.ivanzlotnikov.phonebook.auth.service;

import com.ivanzlotnikov.phonebook.auth.entity.User;
import java.util.Collection;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Адаптер для преобразования сущности User в UserDetails Spring Security.
 * Реализует интерфейс UserDetails для интеграции пользовательской модели с системой безопасности.
 */
@RequiredArgsConstructor
public class UserDetailsAdapter implements UserDetails {

    private final User user;

    /**
     * Возвращает права доступа пользователя.
     *
     * @return коллекцию прав доступа
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

}

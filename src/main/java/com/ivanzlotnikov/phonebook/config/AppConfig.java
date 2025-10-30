package com.ivanzlotnikov.phonebook.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Конфигурация Spring MVC приложения.
 * Настраивает обработку статических ресурсов и WebJars.
 */
@Configuration
public class AppConfig implements WebMvcConfigurer {
    /**
     * Настраивает обработчики статических ресурсов.
     * Регистрирует маппинг для WebJars библиотек (Bootstrap, jQuery и др.).
     *
     * @param registry реестр обработчиков ресурсов
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/webjars/**")
            .addResourceLocations("/webjars/")
            .resourceChain(false);
    }
}

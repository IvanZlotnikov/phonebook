package com.ivanzlotnikov.phone_book.phonebook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс Spring Boot приложения "Телефонный справочник".
 * Точка входа в приложение, инициализирует Spring контекст и запускает веб-сервер.
 */
@SpringBootApplication
public class PhonebookApplication {

	/**
	 * Главный метод приложения.
	 *
	 * @param args аргументы командной строки
	 */
	public static void main(String[] args) {
		SpringApplication.run(PhonebookApplication.class, args);
	}
}

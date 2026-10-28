package com.pesonary.ewalletapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Pesonary e-wallet API. Starts the Spring Boot application.
 * <p>{@code @SpringBootApplication} scans {@code com.pesonary.ewalletapi} and every package below it, so every controller, service, repository and security class must live under this package.
 *
 * @author Akaraan
 */
@SpringBootApplication
public class PesonaryApiApplication {

	/**
	 * Starts the application.
	 * <p><b>Pre-conditions:</b> MySQL is running; {@code DB_USERNAME}, {@code DB_PASSWORD} and {@code JWT_SECRET} are set (or their local defaults work).
	 * <p><b>Post-conditions:</b> the server listens on port 8080; {@code pesonary_db} and its tables exist.
	 *
	 * @param args command-line arguments, passed to Spring
	 */
	public static void main(String[] args) {
		SpringApplication.run(PesonaryApiApplication.class, args);
	}

}

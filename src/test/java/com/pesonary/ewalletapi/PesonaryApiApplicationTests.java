package com.pesonary.ewalletapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test: the full Spring context starts.
 * <p>Uses {@code src/test/resources/application.properties} (H2), so MySQL is not needed.
 *
 * @author Akaraan
 */
@SpringBootTest
class PesonaryApiApplicationTests {

	/**
	 * Passes if every bean is created without errors.
	 * <p><b>Pre-conditions:</b> test properties provide a datasource and {@code app.jwt.secret}.
	 * <p><b>Post-conditions:</b> the context starts; nothing is asserted beyond that.
	 */
	@Test
	void contextLoads() {
	}

}

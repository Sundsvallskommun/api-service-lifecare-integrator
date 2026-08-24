package se.sundsvall.lifecareintegrator.integration.employee.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.lifecareintegrator.Application;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class EmployeePropertiesTest {

	@Autowired
	private EmployeeProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.url()).isEqualTo("http://employee.local");
		assertThat(properties.domain()).isEqualTo("personal");
		assertThat(properties.connectTimeout()).isEqualTo(5);
		assertThat(properties.readTimeout()).isEqualTo(10);
	}
}

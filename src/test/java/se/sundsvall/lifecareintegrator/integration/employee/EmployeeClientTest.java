package se.sundsvall.lifecareintegrator.integration.employee;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeClientTest {

	@Test
	void anUnknownLoginNameIsDismissedRatherThanCountedAsAFailure() {
		// Many Lifecare caseworker ids are not employees. Without dismiss404 every one of them would be an error that the
		// circuit breaker counts.
		assertThat(EmployeeClient.class.getAnnotation(FeignClient.class).dismiss404()).isTrue();
	}
}

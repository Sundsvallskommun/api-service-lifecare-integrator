package se.sundsvall.lifecareintegrator.integration.professionalweb;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.dept44.exception.ClientProblem;
import se.sundsvall.dept44.exception.ServerProblem;
import se.sundsvall.lifecareintegrator.Application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebApi2Configuration.CLIENT_ID;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class ProfessionalWebReminderClientCircuitBreakerTest {

	@Autowired
	private CircuitBreakerRegistry circuitBreakerRegistry;

	@Test
	void theCircuitBreakerIgnoresClientProblemsButNotServerProblems() {
		final var ignore = circuitBreakerRegistry.circuitBreaker(CLIENT_ID).getCircuitBreakerConfig().getIgnoreExceptionPredicate();

		assertThat(ignore.test(new ClientProblem(NOT_FOUND, "Lifecare found nothing there"))).isTrue();
		assertThat(ignore.test(new ServerProblem(BAD_GATEWAY, "Lifecare answered with status 500"))).isFalse();
	}
}

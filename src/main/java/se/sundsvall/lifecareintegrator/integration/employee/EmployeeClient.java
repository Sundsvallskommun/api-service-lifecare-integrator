package se.sundsvall.lifecareintegrator.integration.employee;

import generated.se.sundsvall.employee.PortalPersonData;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.lifecareintegrator.integration.employee.configuration.EmployeeConfiguration;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.lifecareintegrator.integration.employee.configuration.EmployeeConfiguration.CLIENT_ID;

/**
 * Feign contract for api-service-employee. Scoped to the single lookup this service needs: a login name to a person's
 * name, so a Lifecare caseworker id can be shown as something a citizen recognises.
 */
@FeignClient(name = CLIENT_ID, url = "${integration.employee.url}", configuration = EmployeeConfiguration.class, dismiss404 = true)
@CircuitBreaker(name = CLIENT_ID)
public interface EmployeeClient {

	/**
	 * Read the portal person data for one login name.
	 *
	 * @param  municipalityId the municipality id
	 * @param  domain         the employee directory the login name belongs to
	 * @param  loginName      the login name, i.e. the Lifecare caseworker id
	 * @return                the person data, or empty when no such login name exists — {@code dismiss404} turns the
	 *                        404 into a normal decode, which dept44's {@code OptionalDecoder} resolves to empty
	 */
	@GetMapping(path = "/{municipalityId}/portalpersondata/{domain}/{loginName}", produces = APPLICATION_JSON_VALUE)
	Optional<PortalPersonData> getPortalPersonData(
		@PathVariable final String municipalityId,
		@PathVariable final String domain,
		@PathVariable final String loginName);
}

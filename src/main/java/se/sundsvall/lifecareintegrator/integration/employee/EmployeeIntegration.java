package se.sundsvall.lifecareintegrator.integration.employee;

import generated.se.sundsvall.employee.PortalPersonData;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.lifecareintegrator.integration.employee.configuration.EmployeeProperties;

import static java.util.function.Predicate.not;
import static se.sundsvall.lifecareintegrator.util.LogSanitizer.describe;

/**
 * Wrapper around {@link EmployeeClient} that turns a Lifecare caseworker id into a display name.
 *
 * <p>
 * Every failure is absorbed: a name is decoration on a decision, so a decision is never withheld because the directory
 * was unreachable or did not know the id. Only {@code fullname} is read — the response also carries an address, a
 * phone number and a person id, none of which belong in this service's output or logs.
 */
@Component
public class EmployeeIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(EmployeeIntegration.class);

	private final EmployeeClient employeeClient;
	private final EmployeeProperties properties;

	public EmployeeIntegration(final EmployeeClient employeeClient, final EmployeeProperties properties) {
		this.employeeClient = employeeClient;
		this.properties = properties;
	}

	/**
	 * The full name behind a login name.
	 *
	 * @param  municipalityId the municipality id
	 * @param  loginName      the login name, i.e. the Lifecare caseworker id
	 * @return                the full name, or empty when it cannot be resolved for any reason
	 */
	public Optional<String> getFullName(final String municipalityId, final String loginName) {
		try {
			return employeeClient.getPortalPersonData(municipalityId, properties.domain(), loginName)
				.map(PortalPersonData::getFullname)
				.filter(not(String::isBlank));
		} catch (final Exception e) {
			LOG.warn("Could not resolve caseworker '{}': {}", loginName, describe(e));
			return Optional.empty();
		}
	}
}

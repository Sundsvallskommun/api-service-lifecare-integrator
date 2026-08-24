package se.sundsvall.lifecareintegrator.integration.employee.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

/**
 * Builds the {@link se.sundsvall.lifecareintegrator.integration.employee.EmployeeClient} customizer. Authentication is
 * the platform's OAuth2 client credentials flow, unlike the Lifecare integrations.
 *
 * <p>
 * An unknown login name is handled by {@code dismiss404} on the client, which returns null without reaching this error
 * decoder — many Lifecare caseworker ids are not employees at all.
 */
@Import(FeignConfiguration.class)
@EnableConfigurationProperties(EmployeeProperties.class)
public class EmployeeConfiguration {

	public static final String CLIENT_ID = "employee";

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(final EmployeeProperties properties, final ClientRegistrationRepository clientRegistrationRepository) {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(new ProblemErrorDecoder(CLIENT_ID))
			.withRequestTimeoutsInSeconds(properties.connectTimeout(), properties.readTimeout())
			.withRetryableOAuth2InterceptorForClientRegistration(clientRegistrationRepository.findByRegistrationId(CLIENT_ID))
			.composeCustomizersToOne();
	}
}

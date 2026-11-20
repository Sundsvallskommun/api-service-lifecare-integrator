package se.sundsvall.lifecareintegrator.integration.professionalweb.session;

import java.time.Duration;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.sundsvall.dept44.security.Truststore;
import se.sundsvall.lifecareintegrator.integration.LifecareOkHttpClientFactory;
import se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebPrintProperties;
import se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebProperties;

/**
 * Registers the ProfessionalWeb properties, the print templates, and the raw HTTP client that sign-in and the exchange
 * pass-through use. The api2 Feign clients have their own configuration.
 */
@Configuration
@EnableConfigurationProperties({
	ProfessionalWebProperties.class, ProfessionalWebPrintProperties.class
})
class ProfessionalWebConfiguration {

	@Bean
	OkHttpClient professionalWebHttpClient(final ProfessionalWebProperties properties, final Truststore truststore) {
		return LifecareOkHttpClientFactory.http11OkHttp(truststore).newBuilder()
			// Redirects are walked by hand, carrying the cookies from one hop to the next
			.followRedirects(false)
			.followSslRedirects(false)
			.connectTimeout(Duration.ofSeconds(properties.connectTimeout()))
			.readTimeout(Duration.ofSeconds(properties.readTimeout()))
			.build();
	}
}

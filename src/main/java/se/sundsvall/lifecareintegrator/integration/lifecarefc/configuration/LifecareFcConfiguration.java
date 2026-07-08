package se.sundsvall.lifecareintegrator.integration.lifecarefc.configuration;

import feign.Logger;
import feign.RequestTemplate;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Builds the {@link se.sundsvall.caremanagement.lifecare.integration.LifecareFcClient} customizer. FC authenticates
 * with a {@code domain} + {@code key}, both required as query parameters; the spec also accepts the key as an
 * {@code X-API-Key} header, so we
 * send both. The header is harmless where ignored and lets us drop the query-string key once Tieto confirms header auth
 * fleet-wide.
 *
 * <p>
 * Feign logging is forced to {@link Logger.Level#NONE}, overriding the dept44 default of {@code FULL}. FC reads carry
 * the applicant's {@code personId} and the {@code key} secret as query parameters and return income/calculation
 * payloads as bodies; at any
 * level above {@code NONE} Feign would log the request URL (personnummer + secret) and/or the bodies as soon as the
 * client logger is raised to {@code DEBUG}. Pinning it to {@code NONE} keeps that impossible regardless of the
 * configured log level.
 */
@Import(FeignConfiguration.class)
@EnableConfigurationProperties(LifecareFcProperties.class)
public class LifecareFcConfiguration {

	public static final String CLIENT_ID = "lifecare-fc";

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(final LifecareFcProperties properties) {
		return FeignMultiCustomizer.create()
			// Bypass 404 so a "no such resource" surfaces as NOT_FOUND (handled by LifecareFcIntegration) instead of the
			// default BAD_GATEWAY — e.g. a person or document content that does not exist.
			.withErrorDecoder(new ProblemErrorDecoder(CLIENT_ID, List.of(NOT_FOUND.value())))
			.withRequestInterceptor(template -> addAuthentication(template, properties))
			.withCustomizer(builder -> builder.logLevel(Logger.Level.valueOf(properties.logLevel())))
			.withRequestTimeoutsInSeconds(properties.connectTimeout(), properties.readTimeout())
			.composeCustomizersToOne();
	}

	private static void addAuthentication(final RequestTemplate template, final LifecareFcProperties properties) {
		template.query("domain", properties.domain());
		template.query("key", properties.key());
		template.header("X-API-Key", properties.key());
	}
}

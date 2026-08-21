package se.sundsvall.lifecareintegrator.integration.lifecareec.configuration;

import feign.Client;
import feign.Logger;
import feign.RequestTemplate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.security.Truststore;
import se.sundsvall.lifecareintegrator.integration.LifecareErrorDecoder;
import se.sundsvall.lifecareintegrator.integration.LifecareOkHttpClientFactory;

/**
 * Builds the {@link se.sundsvall.lifecareintegrator.integration.lifecareec.LifecareEcClient} customizer. EC
 * authenticates with a {@code domain} + {@code key}, both required as query parameters.
 *
 * <p>
 * An {@code X-API-Key} header carrying the same key used to be sent alongside them, on the assumption that the gateway
 * accepted it and would let us drop the query-string key later. That was never confirmed, and the working Postman call
 * sends no such header — so it is gone. Do not reintroduce it without a response from Tieto: an unexpected credential
 * header is a plausible cause of a 4xx that says nothing useful.
 *
 * <p>
 * Feign logging is forced to {@link feign.Logger.Level#NONE}, overriding the dept44 default of {@code FULL}. EC reads
 * carry the citizen's {@code personId} and the {@code key} secret as query parameters and return care/execution
 * payloads as bodies; at any
 * level above {@code NONE} Feign would log the request URL (personnummer + secret) and/or the bodies as soon as the
 * client logger is raised to {@code DEBUG}. Pinning it to {@code NONE} keeps that impossible regardless of the
 * configured log level.
 */
@Import(FeignConfiguration.class)
@EnableConfigurationProperties(LifecareEcProperties.class)
public class LifecareEcConfiguration {

	public static final String CLIENT_ID = "lifecare-ec";

	/**
	 * Overrides the dept44 {@code okHttpClient} bean for this Feign client only, pinning the connection to HTTP/1.1 —
	 * see {@link LifecareOkHttpClientFactory} for why Lifecare's IIS requires it.
	 */
	@Bean
	Client okHttpClient(final Truststore truststore) {
		return LifecareOkHttpClientFactory.http11Client(truststore);
	}

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(final LifecareEcProperties properties) {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(new LifecareErrorDecoder(CLIENT_ID))
			.withRequestInterceptor(template -> addAuthentication(template, properties))
			.withCustomizer(builder -> builder.logLevel(Logger.Level.valueOf(properties.logLevel())))
			.withRequestTimeoutsInSeconds(properties.connectTimeout(), properties.readTimeout())
			.composeCustomizersToOne();
	}

	private static void addAuthentication(final RequestTemplate template, final LifecareEcProperties properties) {
		queryOnce(template, "domain", properties.domain());
		queryOnce(template, "key", properties.key());
	}

	/**
	 * Add a query parameter unless the template already carries it. Feign re-applies the request interceptors to the
	 * <em>same</em> template on every retry attempt and {@link RequestTemplate#query(String, String...)} appends, so
	 * without this a retried request would go out with {@code domain} and {@code key} repeated once per attempt.
	 */
	private static void queryOnce(final RequestTemplate template, final String name, final String value) {
		if (!template.queries().containsKey(name)) {
			template.query(name, value);
		}
	}
}

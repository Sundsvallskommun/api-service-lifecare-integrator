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
import se.sundsvall.lifecareintegrator.integration.LifecareApiKey;
import se.sundsvall.lifecareintegrator.integration.LifecareErrorDecoder;
import se.sundsvall.lifecareintegrator.integration.LifecareOkHttpClientFactory;

/**
 * Builds the {@link se.sundsvall.lifecareintegrator.integration.lifecareec.LifecareEcClient} customizer. EC
 * authenticates with a {@code domain} + {@code key}, both as query parameters — and the key travels there and
 * nowhere else.
 *
 * <p>
 * An {@code X-API-Key} header carrying the same key was sent alongside them for a while. EC rejects that: it compares
 * every carrier it can find and fails the request when they disagree, which they must, since the query copy is
 * URL-decoded on arrival and the header copy is not. The rejection is
 * {@code 400 "Invalid argument: key. Ambiguous API key, different keys either in the HTTP header (Authorization) or in
 * the HTTP header (X-API-Key) or in the URL query string (key)."} (verified 2026-08-24). The working Postman call sends
 * the query parameter only. Do not add a second carrier back.
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
		queryOnce(template, "key", LifecareApiKey.forQuery(properties.key()));
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

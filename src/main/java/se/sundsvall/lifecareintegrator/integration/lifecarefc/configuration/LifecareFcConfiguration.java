package se.sundsvall.lifecareintegrator.integration.lifecarefc.configuration;

import feign.Client;
import feign.Logger;
import feign.RequestTemplate;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.security.Truststore;
import se.sundsvall.lifecareintegrator.integration.LifecareErrorDecoder;
import se.sundsvall.lifecareintegrator.integration.LifecareOkHttpClientFactory;

import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Builds the {@link se.sundsvall.lifecareintegrator.integration.lifecarefc.LifecareFcClient} customizer. FC
 * authenticates with a {@code domain} + {@code key}, both required as query parameters. An unconfirmed
 * {@code X-API-Key} header used to be sent alongside them; it was removed with the EC one — see
 * {@link se.sundsvall.lifecareintegrator.integration.lifecareec.configuration.LifecareEcConfiguration}.
 *
 * <p>
 * The key is chosen per request: FC licences the {@code Users/*} directory to its own consumer, separate from the
 * person-based case APIs, so requests into {@code /Users} are authenticated with
 * {@link LifecareFcProperties#userKeyOrDefault()} and everything else with {@link LifecareFcProperties#key()}. Where no
 * separate user key is configured both resolve to the same key.
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

	/** The path segment marking the separately licensed FC user directory ({@code /apifc/v1/Users/*}). */
	static final String USERS_PATH_SEGMENT = "/Users";

	/**
	 * Overrides the dept44 {@code okHttpClient} bean for this Feign client only, pinning the connection to HTTP/1.1 —
	 * see {@link LifecareOkHttpClientFactory} for why Lifecare's IIS requires it.
	 */
	@Bean
	Client okHttpClient(final Truststore truststore) {
		return LifecareOkHttpClientFactory.http11Client(truststore);
	}

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(final LifecareFcProperties properties) {
		return FeignMultiCustomizer.create()
			// Bypass 404 so a "no such resource" surfaces as NOT_FOUND (handled by LifecareFcIntegration) instead of the
			// default BAD_GATEWAY — e.g. a person or document content that does not exist.
			.withErrorDecoder(new LifecareErrorDecoder(CLIENT_ID, List.of(NOT_FOUND.value())))
			.withRequestInterceptor(template -> addAuthentication(template, properties))
			.withCustomizer(builder -> builder.logLevel(Logger.Level.valueOf(properties.logLevel())))
			.withRequestTimeoutsInSeconds(properties.connectTimeout(), properties.readTimeout())
			.composeCustomizersToOne();
	}

	private static void addAuthentication(final RequestTemplate template, final LifecareFcProperties properties) {
		final var key = keyFor(template.path(), properties);

		queryOnce(template, "domain", properties.domain());
		queryOnce(template, "key", key);
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

	/** The licence key the given request path is authenticated with — see the class documentation. */
	static String keyFor(final String path, final LifecareFcProperties properties) {
		if (ofNullable(path).orElse("").contains(USERS_PATH_SEGMENT)) {
			return properties.userKeyOrDefault();
		}
		return properties.key();
	}
}

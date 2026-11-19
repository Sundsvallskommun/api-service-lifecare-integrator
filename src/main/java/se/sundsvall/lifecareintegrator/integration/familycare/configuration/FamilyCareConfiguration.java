package se.sundsvall.lifecareintegrator.integration.familycare.configuration;

import feign.Client;
import feign.Logger;
import feign.RequestTemplate;
import java.net.URLEncoder;
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Import(FeignConfiguration.class)
@EnableConfigurationProperties(FamilyCareProperties.class)
public class FamilyCareConfiguration {

	public static final String CLIENT_ID = "familycare";

	static final String USERS_PATH_SEGMENT = "/Users";

	@Bean
	Client okHttpClient(final Truststore truststore) {
		return LifecareOkHttpClientFactory.http11Client(truststore);
	}

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(final FamilyCareProperties properties) {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(new LifecareErrorDecoder(CLIENT_ID, List.of(NOT_FOUND.value())))
			.withRequestInterceptor(template -> addAuthentication(template, properties))
			.withCustomizer(builder -> builder.logLevel(Logger.Level.valueOf(properties.logLevel())))
			.withRequestTimeoutsInSeconds(properties.connectTimeout(), properties.readTimeout())
			.composeCustomizersToOne();
	}

	private static void addAuthentication(final RequestTemplate template, final FamilyCareProperties properties) {
		template.query("domain", properties.domain());
		template.query("key", toQueryValue(keyFor(template.path(), properties)));
	}

	static String toQueryValue(final String key) {
		// URLEncoder renders a space as '+', which Lifecare reads back as a space
		return URLEncoder.encode(key, UTF_8).replace("+", "%20");
	}

	static String keyFor(final String path, final FamilyCareProperties properties) {
		if (ofNullable(path).orElse("").contains(USERS_PATH_SEGMENT)) {
			return properties.userKeyOrDefault();
		}
		return properties.key();
	}
}

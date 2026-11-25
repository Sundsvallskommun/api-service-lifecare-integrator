package se.sundsvall.lifecareintegrator.integration.familycare.configuration;

import feign.Client;
import feign.Logger;
import feign.RequestTemplate;
import java.util.List;
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

@Import(FeignConfiguration.class)
public class FamilyCareConfiguration {

	public static final String CLIENT_ID = "familycare";

	static final String USERS_PATH_SEGMENT = "/Users";

	@Bean
	Client okHttpClient(final Truststore truststore) {
		return LifecareOkHttpClientFactory.createHttp11Client(truststore);
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
		template.query("key", selectKey(template.path(), properties));
	}

	static String selectKey(final String path, final FamilyCareProperties properties) {
		if (ofNullable(path).orElse("").contains(USERS_PATH_SEGMENT)) {
			return properties.getUserKeyOrDefault();
		}
		return properties.key();
	}
}

package se.sundsvall.lifecareintegrator.integration.elderlycare.configuration;

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

@Import(FeignConfiguration.class)
@EnableConfigurationProperties(ElderlyCareProperties.class)
public class ElderlyCareConfiguration {

	public static final String CLIENT_ID = "elderlycare";

	@Bean
	Client okHttpClient(final Truststore truststore) {
		return LifecareOkHttpClientFactory.http11Client(truststore);
	}

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(final ElderlyCareProperties properties) {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(new LifecareErrorDecoder(CLIENT_ID))
			.withRequestInterceptor(template -> addAuthentication(template, properties))
			.withCustomizer(builder -> builder.logLevel(Logger.Level.valueOf(properties.logLevel())))
			.withRequestTimeoutsInSeconds(properties.connectTimeout(), properties.readTimeout())
			.composeCustomizersToOne();
	}

	private static void addAuthentication(final RequestTemplate template, final ElderlyCareProperties properties) {
		template.query("domain", properties.domain());
		template.query("key", properties.key());
	}
}

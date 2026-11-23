package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import feign.Client;
import feign.Logger;
import feign.Request;
import feign.codec.Encoder;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.security.Truststore;
import se.sundsvall.lifecareintegrator.integration.LifecareOkHttpClientFactory;
import se.sundsvall.lifecareintegrator.integration.professionalweb.session.ProfessionalWebSession;

import static java.util.concurrent.TimeUnit.SECONDS;

@Import(FeignConfiguration.class)
public class ProfessionalWebApi2Configuration {

	public static final String CLIENT_ID = "lifecare-professionalweb";

	@Bean
	Client okHttpClient(final Truststore truststore) {
		return new ProfessionalWebHtmlBufferingClient(new ProfessionalWebEmptyQueryValueClient(LifecareOkHttpClientFactory.http11Client(truststore)));
	}

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(final ProfessionalWebProperties properties, final ProfessionalWebSession session) {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(new ProfessionalWebErrorDecoder())
			.withRequestInterceptor(new ProfessionalWebSessionInterceptor(properties, session))
			// Redirects are read, not followed: one to the identity portal means the session is gone
			.withRequestOptions(new Request.Options(properties.connectTimeout(), SECONDS, properties.readTimeout(), SECONDS, false))
			.withCustomizer(builder -> builder
				.logLevel(Logger.Level.NONE)
				// Every body is bytes sent as they are. dept44's form encoder would post a byte[] form as an empty one
				.encoder(new Encoder.Default())
				.responseInterceptor(new ProfessionalWebSessionResponseInterceptor(session))
				.retryer(new ProfessionalWebSessionRetryer(session)))
			.composeCustomizersToOne();
	}
}

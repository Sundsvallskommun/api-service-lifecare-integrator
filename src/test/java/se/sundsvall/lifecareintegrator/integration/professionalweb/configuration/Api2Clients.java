package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import feign.Feign;
import feign.okhttp.OkHttpClient;
import java.time.Clock;
import java.time.Duration;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import se.sundsvall.lifecareintegrator.integration.professionalweb.session.ProfessionalWebHttp;
import se.sundsvall.lifecareintegrator.integration.professionalweb.session.ProfessionalWebSession;
import se.sundsvall.lifecareintegrator.integration.professionalweb.session.ProfessionalWebSignIn;

/**
 * Builds an api2 Feign client the way the application does, against a test server, on a configured session so no
 * sign-in is needed.
 */
public final class Api2Clients {

	public static final String SESSION_COOKIE = "ASP.NET_SessionId=s; LEGACY-TOKEN=tok";

	private Api2Clients() {}

	public static <T> T client(final Class<T> type, final String baseUrl) {
		final var properties = new ProfessionalWebProperties(baseUrl, "Domain", "Actor_Professional", "saml", "Sundsvall_Intra",
			"user", "pw", Duration.ofMinutes(20), 1, 5, SESSION_COOKIE, Duration.ofMinutes(5));
		final var http = new ProfessionalWebHttp(new okhttp3.OkHttpClient.Builder().followRedirects(false).followSslRedirects(false).build());
		final var session = new ProfessionalWebSession(properties, new ProfessionalWebSignIn(properties, http), http, Clock.systemUTC());
		final var builder = Feign.builder()
			.contract(new SpringMvcContract())
			.client(new ProfessionalWebHtmlBufferingClient(new OkHttpClient()));
		new ProfessionalWebApi2Configuration().feignBuilderCustomizer(properties, session).customize(builder);
		return builder.target(type, baseUrl);
	}
}

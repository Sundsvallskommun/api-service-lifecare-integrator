package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import se.sundsvall.lifecareintegrator.integration.professionalweb.session.ProfessionalWebHttp;
import se.sundsvall.lifecareintegrator.integration.professionalweb.session.ProfessionalWebSession;

import static feign.Request.HttpMethod.GET;

class ProfessionalWebSessionInterceptor implements RequestInterceptor {

	private final ProfessionalWebProperties properties;
	private final ProfessionalWebSession session;

	ProfessionalWebSessionInterceptor(final ProfessionalWebProperties properties, final ProfessionalWebSession session) {
		this.properties = properties;
		this.session = session;
	}

	@Override
	public void apply(final RequestTemplate template) {
		final var headers = new LinkedHashMap<String, String>();
		headers.putAll(ProfessionalWebHttp.BROWSER_HEADERS);
		headers.putAll(ProfessionalWebHttp.AJAX_HEADERS);
		headers.put("Origin", toOrigin(properties.baseUrl()));
		headers.put("Referer", properties.baseUrl() + "/WE.Flow.Html/");
		if (template.method() != null && !GET.name().equals(template.method())) {
			headers.put("Content-Type", "application/json; charset=UTF-8");
		}
		headers.putAll(session.prepare());

		// Feign runs the interceptors again on the same template for every retry, and header() appends
		headers.forEach((name, value) -> replaceHeader(template, name, value));
	}

	private static void replaceHeader(final RequestTemplate template, final String name, final String value) {
		template.header(name, List.of());
		template.header(name, value);
	}

	private static String toOrigin(final String baseUrl) {
		final var uri = URI.create(baseUrl);
		return uri.getScheme() + "://" + uri.getRawAuthority();
	}
}

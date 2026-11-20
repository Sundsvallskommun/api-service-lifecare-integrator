package se.sundsvall.lifecareintegrator.integration.professionalweb;

import feign.Client;
import feign.Request;
import feign.Response;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

class ProfessionalWebFeignClient implements Client {

	private final ProfessionalWebExchange exchange;

	ProfessionalWebFeignClient(final ProfessionalWebExchange exchange) {
		this.exchange = exchange;
	}

	@Override
	public Response execute(final Request request, final Request.Options options) {
		final var uri = URI.create(request.url());
		final var answer = exchange.exchange(request.httpMethod().name(), uri.getPath(), toQueryParams(uri), request.body());
		return Response.builder()
			.request(request)
			.status(answer.status())
			.headers(new LinkedHashMap<String, Collection<String>>(answer.headers().map()))
			.body(answer.body())
			.build();
	}

	static Map<String, String> toQueryParams(final URI uri) {
		final var params = new LinkedHashMap<String, String>();
		if (uri.getRawQuery() == null) {
			return params;
		}
		for (final var pair : uri.getRawQuery().split("&")) {
			final var separator = pair.indexOf('=');
			if (separator < 0) {
				params.put(URLDecoder.decode(pair, UTF_8), "");
				continue;
			}
			params.put(URLDecoder.decode(pair.substring(0, separator), UTF_8), URLDecoder.decode(pair.substring(separator + 1), UTF_8));
		}
		return params;
	}
}

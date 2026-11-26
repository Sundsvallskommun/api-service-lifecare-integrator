package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import feign.Client;
import feign.Request;
import feign.Response;
import java.io.IOException;
import java.util.Arrays;

import static java.util.stream.Collectors.joining;

/**
 * Feign sends a query parameter with an empty value as its bare name, without "=". Lifecare's web app sends "name=",
 * so the "=" is put back before the request goes out.
 */
class ProfessionalWebEmptyQueryValueClient implements Client {

	private final Client delegate;

	ProfessionalWebEmptyQueryValueClient(final Client delegate) {
		this.delegate = delegate;
	}

	@Override
	public Response execute(final Request request, final Request.Options options) throws IOException {
		return delegate.execute(toRequestWithEmptyValues(request), options);
	}

	private static Request toRequestWithEmptyValues(final Request request) {
		final var queryStart = request.url().indexOf('?');
		if (queryStart < 0) {
			return request;
		}
		final var query = Arrays.stream(request.url().substring(queryStart + 1).split("&"))
			.map(ProfessionalWebEmptyQueryValueClient::toParameterWithValue)
			.collect(joining("&"));
		final var url = request.url().substring(0, queryStart + 1) + query;
		return Request.create(request.httpMethod(), url, request.headers(), request.body(), request.charset(), request.requestTemplate());
	}

	private static String toParameterWithValue(final String parameter) {
		if (parameter.isEmpty() || parameter.contains("=")) {
			return parameter;
		}
		return parameter + "=";
	}
}

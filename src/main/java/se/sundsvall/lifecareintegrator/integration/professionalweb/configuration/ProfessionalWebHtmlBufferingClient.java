package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import feign.Client;
import feign.Request;
import feign.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Reads an HTML answer into memory, so the session response interceptor can tell a print's parameter questions from a
 * lost session and the decoder can still read the page afterwards. Every other answer streams as it came.
 */
class ProfessionalWebHtmlBufferingClient implements Client {

	private final Client delegate;

	ProfessionalWebHtmlBufferingClient(final Client delegate) {
		this.delegate = delegate;
	}

	@Override
	public Response execute(final Request request, final Request.Options options) throws IOException {
		final var response = delegate.execute(request, options);
		if (response.body() == null || !isHtml(response)) {
			return response;
		}
		try (response; InputStream stream = response.body().asInputStream()) {
			return response.toBuilder().body(stream.readAllBytes()).build();
		}
	}

	private static boolean isHtml(final Response response) {
		return Optional.ofNullable(response.headers().get("Content-Type")).orElse(List.of()).stream()
			.anyMatch(type -> type.contains("text/html"));
	}
}

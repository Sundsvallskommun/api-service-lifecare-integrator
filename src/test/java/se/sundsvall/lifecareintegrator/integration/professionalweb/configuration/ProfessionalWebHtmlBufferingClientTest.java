package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import feign.Client;
import feign.Request;
import feign.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class ProfessionalWebHtmlBufferingClientTest {

	private static final Request REQUEST = Request.create(Request.HttpMethod.GET, "http://lifecare/x", Map.of(), null, UTF_8, null);
	private static final Request.Options OPTIONS = new Request.Options();

	@Test
	void htmlAnswerCanBeReadMoreThanOnce() throws IOException {
		final var client = new ProfessionalWebHtmlBufferingClient(answering("text/html; charset=utf-8", "<html>åäö</html>"));

		final var response = client.execute(REQUEST, OPTIONS);

		assertThat(read(response)).isEqualTo("<html>åäö</html>");
		assertThat(read(response)).isEqualTo("<html>åäö</html>");
		assertThat(response.status()).isEqualTo(200);
		assertThat(response.headers().get("Content-Type")).containsExactly("text/html; charset=utf-8");
	}

	@Test
	void otherAnswersStreamAsTheyCame() throws IOException {
		final var answer = response("application/pdf", "%PDF");
		final var client = new ProfessionalWebHtmlBufferingClient((request, options) -> answer);

		assertThat(client.execute(REQUEST, OPTIONS)).isSameAs(answer);
	}

	@Test
	void answerWithoutBodyIsLeftAlone() throws IOException {
		final var answer = Response.builder().status(204).request(REQUEST).headers(Map.of("Content-Type", List.of("text/html"))).build();
		final var client = new ProfessionalWebHtmlBufferingClient((request, options) -> answer);

		assertThat(client.execute(REQUEST, OPTIONS)).isSameAs(answer);
	}

	private static Client answering(final String contentType, final String body) {
		return (request, options) -> response(contentType, body);
	}

	private static Response response(final String contentType, final String body) {
		// One stream, as OkHttp hands it over: it can be read once
		final InputStream once = new ByteArrayInputStream(body.getBytes(UTF_8));
		return Response.builder().status(200).request(REQUEST)
			.headers(Map.of("Content-Type", List.of(contentType)))
			.body(once, null)
			.build();
	}

	private static String read(final Response response) throws IOException {
		try (var stream = response.body().asInputStream()) {
			return new String(stream.readAllBytes(), UTF_8);
		}
	}
}

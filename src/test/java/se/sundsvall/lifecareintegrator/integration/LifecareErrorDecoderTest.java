package se.sundsvall.lifecareintegrator.integration;

import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.exception.ClientProblem;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.lifecareintegrator.integration.LifecareErrorDecoder.withRepeatableBody;

class LifecareErrorDecoderTest {

	private static final String PROBLEM_BODY = """
		{"title":"Invalid query format","status":400,"detail":"unsupported filter"}""";

	@Test
	void keepsTheErrorMessageThatDept44WouldOtherwiseLose() {
		// dept44's AbstractErrorDecoder reads the body twice; with a one-shot stream the second read comes back empty
		// and the message degrades to "Unknown error". Buffering first is what makes the real title survive.
		final var problem = new LifecareErrorDecoder("lifecare-ec")
			.decode("LifecareEcClient#getSolDecisions(String,Integer)", oneShotResponse(400, PROBLEM_BODY));

		assertThat(problem).isInstanceOf(ClientProblem.class);
		assertThat(problem.getMessage())
			.contains("Invalid query format")
			.doesNotContain("Unknown error");
	}

	@Test
	void bypassedStatusIsStillHonoured() {
		final var problem = new LifecareErrorDecoder("lifecare-fc", List.of(NOT_FOUND.value()))
			.decode("LifecareFcClient#getPerson(String)", oneShotResponse(404, PROBLEM_BODY));

		assertThat(problem).isInstanceOf(ClientProblem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND);
	}

	@Test
	void loggingTheBodyDoesNotConsumeItForTheDecoder() {
		// The snippet is logged from the same body the decoder then parses; if reading it were destructive the message
		// would come back empty — which is the whole failure this class exists to prevent.
		final var problem = new LifecareErrorDecoder("lifecare-ec")
			.decode("LifecareEcClient#getSolDecisions(String,Integer)", oneShotResponse(400, PROBLEM_BODY));

		assertThat(problem.getMessage()).contains("Invalid query format");
	}

	@Test
	void anUnbufferedBodyIsLeftUnreadRatherThanConsumed() throws IOException {
		// withRepeatableBody gives up on an unreadable body; the snippet must not spend its single pass.
		final var unbuffered = oneShotResponse(400, PROBLEM_BODY);

		assertThat(LifecareErrorDecoder.bodySnippet(unbuffered)).isEqualTo("<unbuffered>");
		assertThat(read(unbuffered.body())).isEqualTo(PROBLEM_BODY);
	}

	@Test
	void bodySnippetReportsAbsentAndEmptyBodies() {
		final var bodiless = Response.builder().status(401).request(request()).build();
		assertThat(LifecareErrorDecoder.bodySnippet(bodiless)).isEqualTo("<none>");

		final var empty = Response.builder().status(400).request(request()).body("   ", UTF_8).build();
		assertThat(LifecareErrorDecoder.bodySnippet(empty)).isEqualTo("<empty>");
	}

	@Test
	void bodySnippetRedactsAndCapsWhatItLogs() {
		final var long_ = Response.builder()
			.status(400)
			.request(request())
			.body("{\"Message\":\"no such person 19900101TF03\"}" + "x".repeat(3000), UTF_8)
			.build();

		assertThat(LifecareErrorDecoder.bodySnippet(long_))
			.doesNotContain("19900101TF03")
			.contains("[REDACTED-PNR]")
			.endsWith("…(truncated)")
			.hasSizeLessThan(2100);
	}

	@Test
	void bufferedBodyCanBeReadMoreThanOnce() throws IOException {
		final var buffered = withRepeatableBody(oneShotResponse(400, PROBLEM_BODY)).body();

		assertThat(buffered.isRepeatable()).isTrue();
		assertThat(read(buffered)).isEqualTo(PROBLEM_BODY);
		assertThat(read(buffered)).isEqualTo(PROBLEM_BODY);
	}

	@Test
	void aOneShotBodyReallyIsOneShot() throws IOException {
		// Guards the fixture itself: if this ever passes twice, the test above proves nothing.
		final var oneShot = oneShotResponse(400, PROBLEM_BODY).body();

		assertThat(read(oneShot)).isEqualTo(PROBLEM_BODY);
		assertThat(read(oneShot)).isEmpty();
	}

	@Test
	void alreadyRepeatableAndBodilessResponsesArePassedThrough() {
		final var repeatable = Response.builder()
			.status(400)
			.request(request())
			.body(PROBLEM_BODY, UTF_8)
			.build();
		assertThat(withRepeatableBody(repeatable)).isSameAs(repeatable);

		final var bodiless = Response.builder()
			.status(401)
			.request(request())
			.build();
		assertThat(withRepeatableBody(bodiless)).isSameAs(bodiless);
	}

	private static String read(final Response.Body body) throws IOException {
		return new String(body.asInputStream().readAllBytes(), UTF_8);
	}

	private static Request request() {
		return Request.create(Request.HttpMethod.GET,
			"https://lifecare-test.sundsvall.se/WE.EC.Integration.Host/api/v1/sol_decisions"
				+ "?q=PersonId='19900101TF03'&limit=1000&domain=the-domain&key=the-secret",
			Map.of(), null, new RequestTemplate());
	}

	/** A response whose body may only be read once, like the one OkHttp hands Feign. */
	private static Response oneShotResponse(final int status, final String body) {
		return Response.builder()
			.status(status)
			.request(request())
			.body(new OneShotBody(body))
			.build();
	}

	private static final class OneShotBody implements Response.Body {

		private final byte[] content;
		private final InputStream stream;

		private OneShotBody(final String content) {
			this.content = content.getBytes(UTF_8);
			this.stream = new ByteArrayInputStream(this.content);
		}

		@Override
		public Integer length() {
			return content.length;
		}

		@Override
		public boolean isRepeatable() {
			return false;
		}

		@Override
		public InputStream asInputStream() {
			return stream;
		}

		@Override
		public Reader asReader(final Charset charset) {
			return new InputStreamReader(stream, charset);
		}

		@Override
		public void close() {
			// nothing to release
		}
	}
}

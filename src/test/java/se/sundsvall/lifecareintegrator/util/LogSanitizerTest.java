package se.sundsvall.lifecareintegrator.util;

import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.lifecareintegrator.util.LogSanitizer.describe;
import static se.sundsvall.lifecareintegrator.util.LogSanitizer.redact;

class LogSanitizerTest {

	private static final String FEIGN_MESSAGE = """
		stream was reset: HTTP_1_1_REQUIRED executing GET \
		https://lifecare-test.sundsvall.se/WE.EC.Integration.Host/api/v1/lss_decisions\
		?q=PersonId%3D%2719900101TF03%27&limit=1000&domain=SundsvallVoO_PLUS&key=Gb4zOQpndCXz3XQ%2b7PFGlgvY""";

	@Test
	void redactRemovesTheKeyAndThePersonNumberButKeepsTheFilterGrammar() {
		final var redacted = redact(FEIGN_MESSAGE);

		assertThat(redacted)
			.doesNotContain("19900101TF03", "Gb4zOQpndCXz3XQ")
			.contains("&key=[REDACTED]")
			// The q filter's shape is the diagnostic — only the personnummer inside it goes.
			.contains("?q=PersonId%3D%27[REDACTED-PNR]%27")
			// Everything else is kept: the failure reason, the endpoint and the non-secret parameters.
			.contains("HTTP_1_1_REQUIRED", "/api/v1/lss_decisions", "&limit=1000", "&domain=SundsvallVoO_PLUS");
	}

	@Test
	void redactCatchesPersonNumbersInEveryShapeLifecareUses() {
		assertThat(redact("plain 199001011234, hyphenated 19900101-1234, encoded 19900101%2D1234, test 19900101TF03"))
			.isEqualTo("plain [REDACTED-PNR], hyphenated [REDACTED-PNR], encoded [REDACTED-PNR], test [REDACTED-PNR]");
	}

	@Test
	void redactLeavesNumbersThatAreNotPersonNumbersAlone() {
		assertThat(redact("limit=1000, id 167927, year 2026, offset 19900101"))
			.isEqualTo("limit=1000, id 167927, year 2026, offset 19900101");
	}

	@Test
	void redactIsCaseInsensitiveAndHandlesRepeatedParameters() {
		assertThat(redact("...?q=PersonId%3A19900101TF03&KEY=secret&other=kept&key=secret2"))
			.isEqualTo("...?q=PersonId%3A[REDACTED-PNR]&KEY=[REDACTED]&other=kept&key=[REDACTED]");
	}

	@Test
	void redactLeavesUnrelatedTextAlone() {
		assertThat(redact("no query parameters here")).isEqualTo("no query parameters here");
	}

	@Test
	void redactHandlesNull() {
		assertThat(redact(null)).isNull();
	}

	@Test
	void describeIncludesTheWholeCauseChainRedacted() {
		final var throwable = new IllegalStateException("outer ?key=secret", new IOException(FEIGN_MESSAGE));

		assertThat(describe(throwable))
			.isEqualTo("IllegalStateException: outer ?key=[REDACTED] | caused by: IOException: "
				+ redact(FEIGN_MESSAGE));
	}

	@Test
	void describeHandlesMessagelessAndSelfReferencingThrowables() {
		assertThat(describe(new SelfCausedException())).isEqualTo("SelfCausedException: null");
	}

	/** A throwable that is its own cause — the loop in {@code describe} must not spin on it. */
	private static final class SelfCausedException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		@Override
		public synchronized Throwable getCause() {
			return this;
		}
	}
}

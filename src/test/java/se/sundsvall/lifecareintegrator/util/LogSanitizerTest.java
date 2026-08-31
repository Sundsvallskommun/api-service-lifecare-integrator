package se.sundsvall.lifecareintegrator.util;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

	@Test
	void dept44MasksTheCategoriesItCovers() {
		// Delegated to PiiMasker rather than reimplemented: ten-digit personnummer, phone numbers and e-mail.
		assertThat(redact("pnr 900101-1234, tel 070-123 45 67, mail john.doe@example.com"))
			.doesNotContain("900101-1234", "070-123 45 67", "john.doe@")
			.contains("j***@example.com");
	}

	@Test
	void aPartyIdIsLeftReadable() {
		// A partyId is already opaque — it cannot be resolved to a person without API access — so masking it would
		// cost traceability and buy nothing. PiiMasker.maskUuid is deliberately not applied, and dept44 8.0.10's
		// twelve-digit personnummer shape (which matches the UUID's first two groups) is kept away from it.
		assertThat(redact("partyId 81471222-5798-11e9-ae24-57fa13b361e1 failed"))
			.contains("81471222-5798-11e9-ae24-57fa13b361e1");
	}

	@Test
	void aPersonNumberNextToAPartyIdIsStillMasked() {
		// The UUID carve-out must not shield the text around it.
		assertThat(redact("partyId 81471222-5798-11e9-ae24-57fa13b361e1 pnr 900101-1234 mail john.doe@example.com"))
			.contains("81471222-5798-11e9-ae24-57fa13b361e1")
			.doesNotContain("900101-1234", "john.doe@");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"199001011234",       // twelve digits — caught by this sanitizer before dept44 8.0.10's pattern sees it
		"19900101-1234",      // likewise the hyphenated form
		"19900101TF03",       // the test population's letter suffix, which dept44's \d{4} cannot match
	})
	void theFormsDept44LetsThroughAreStillCovered(final String personNumber) {
		assertThat(redact("person " + personNumber + " failed"))
			.doesNotContain(personNumber)
			.contains("[REDACTED-PNR]");
	}

	@Test
	void aPercentEncodedPersonNumberIsCovered() {
		// dept44's word boundaries cannot fire between the hex digits of the surrounding escapes.
		assertThat(redact("?q=PersonId%3D%27199001011234%27"))
			.doesNotContain("199001011234")
			.contains("[REDACTED-PNR]");
	}

	@Test
	void controlCharactersCannotForgeALogLine() {
		assertThat(redact("body\r\n2026-01-01 ERROR forged line"))
			.doesNotContain("\r", "\n")
			.contains("body  2026-01-01 ERROR forged line");
	}

	@Test
	void swedishTextAndPercentEscapesSurvive() {
		// Both are why dept44's sanitizeForLogging is not used here — it strips non-ASCII and every '%'.
		assertThat(redact("Öx Bifall Äldreboende ?q=PersonId%3D%27x%27"))
			.contains("Öx Bifall Äldreboende")
			.contains("%3D", "%27");
	}
}

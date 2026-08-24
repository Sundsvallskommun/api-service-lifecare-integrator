package se.sundsvall.lifecareintegrator.util;

import java.util.regex.Pattern;
import se.sundsvall.dept44.util.PiiMasker;

import static java.util.Optional.ofNullable;

/**
 * Removes API keys and personal data from text before it is logged.
 *
 * <p>
 * Most of the work is dept44's {@link PiiMasker}; this adds only what it does not cover here. {@code maskUuid} is
 * deliberately not applied — a partyId is already an opaque identifier that cannot be resolved to a person without API
 * access, so masking it would cost traceability and buy nothing.
 *
 * <p>
 * The two supplements are the API key, which is a secret rather than PII and so outside {@code PiiMasker}'s scope, and
 * three personnummer forms it lets through. Against dept44 8.0.9, whose pattern is {@code \b\d{6}[-+]?\d{4}\b}:
 * the twelve-digit form Lifecare uses, hyphenated or not, since the pattern has no century-prefix group; any
 * personnummer inside a percent-encoded query value, where the {@code \b} anchors cannot fire between hex digits; and
 * the letter-suffixed form the test population uses ({@code 19900101TF03}), which {@code \d{4}} cannot match.
 *
 * <p>
 * Later dept44 versions add the century prefix, which closes the first of those; the other two remain.
 *
 * <p>
 * Anything derived from a Lifecare request or response must pass through here — Feign builds its exception messages
 * from the request URL, which carries both a key and a personnummer. The {@code q} filter's grammar is kept while its
 * personnummer is replaced, because the shape of the filter is what a failed call is usually diagnosed from.
 */
public final class LogSanitizer {

	/** The API secret, as a query parameter. Redacted whole — none of it is diagnostic. */
	private static final Pattern SENSITIVE_QUERY_PARAM = Pattern.compile("(?i)([?&]key=)[^&\\s]*");

	/**
	 * A twelve-digit personnummer, with or without separator; the last four characters are loose because test
	 * personnummer use letters there. Must stay unanchored by word boundaries — percent-encoded punctuation surrounding
	 * the number ends in hex digits, which a boundary assertion counts as alphanumeric and so stops matching.
	 */
	private static final Pattern PERSON_NUMBER = Pattern.compile("(?:19|20)\\d{6}(?:%2[Dd]|[-+])?[0-9A-Za-z]{4}");

	private static final String REDACTED_PARAM_VALUE = "$1[REDACTED]";

	private static final String REDACTED_PERSON_NUMBER = "[REDACTED-PNR]";

	/**
	 * Control characters, replaced with a space so a vendor-controlled body cannot forge log lines with CR/LF.
	 *
	 * <p>
	 * dept44's {@code LogUtils.sanitizeForLogging} is the usual tool for this, but it is unusable here: it strips
	 * everything outside printable ASCII along with every {@code %}, which would erase the å/ä/ö from the Swedish error
	 * messages this exists to make readable, and the percent-escapes from the {@code q} filter that a failed EC call is
	 * diagnosed from. Removing control characters is the part that guards against log forging.
	 *
	 * <p>
	 * Spelled out rather than {@code \p{Cntrl}}, which is ASCII-only: NEL and the Unicode line and paragraph
	 * separators are line breaks to some log pipelines and would otherwise pass through.
	 */
	private static final Pattern CONTROL_CHARACTER = Pattern.compile("[\\x00-\\x1F\\x7F\\u0085\\u2028\\u2029]");

	private static final String CAUSE_SEPARATOR = " | caused by: ";

	private LogSanitizer() {}

	/**
	 * Redact the API key and mask personal data. Ten-digit personnummer, phone numbers and e-mail addresses are masked
	 * by {@link PiiMasker} and so carry its mask characters rather than the {@code [REDACTED-…]} markers used here.
	 *
	 * @param  message the text to redact, may be {@code null}
	 * @return         the redacted text, or {@code null} if the message was {@code null}
	 */
	public static String redact(final String message) {
		return ofNullable(message)
			.map(text -> CONTROL_CHARACTER.matcher(text).replaceAll(" "))
			.map(text -> SENSITIVE_QUERY_PARAM.matcher(text).replaceAll(REDACTED_PARAM_VALUE))
			.map(text -> PERSON_NUMBER.matcher(text).replaceAll(REDACTED_PERSON_NUMBER))
			// PiiMasker.maskPii applies phone before personal so one mask cannot bite into the other's match; the same
			// order is kept here.
			.map(PiiMasker::maskPhoneNumber)
			.map(PiiMasker::maskPersonalNumber)
			.map(PiiMasker::maskEmail)
			.orElse(null);
	}

	/**
	 * A redacted, single-line description of a throwable and its cause chain. Use this rather than logging the throwable
	 * itself — a stack trace opens with the unredacted {@code toString()}.
	 *
	 * @param  throwable the throwable to describe
	 * @return           the redacted description
	 */
	public static String describe(final Throwable throwable) {
		final var description = new StringBuilder();

		var current = throwable;
		while (current != null) {
			if (!description.isEmpty()) {
				description.append(CAUSE_SEPARATOR);
			}
			description.append(current.getClass().getSimpleName())
				.append(": ")
				.append(redact(current.getMessage()));

			final var cause = current.getCause();
			if (cause == current) {
				break;
			}
			current = cause;
		}

		return description.toString();
	}
}

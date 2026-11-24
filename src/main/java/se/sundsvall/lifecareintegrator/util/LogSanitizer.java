package se.sundsvall.lifecareintegrator.util;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;
import se.sundsvall.dept44.util.PiiMasker;
import se.sundsvall.dept44.util.PiiMasker.Category;

import static java.util.Optional.ofNullable;
import static se.sundsvall.dept44.util.PiiMasker.Category.EMAIL;
import static se.sundsvall.dept44.util.PiiMasker.Category.PERSONAL_NUMBER;
import static se.sundsvall.dept44.util.PiiMasker.Category.PHONE_NUMBER;

/**
 * Removes the API key and personal data from Lifecare URLs, messages and exceptions before they are logged or returned.
 * Adds to dept44's {@link PiiMasker} only what it misses here.
 */
public final class LogSanitizer {

	private static final Pattern API_KEY_PARAMETER = Pattern.compile("(?i)([?&]key=)[^&\\s]*");
	private static final String REDACTED_API_KEY = "$1[REDACTED]";

	// No word boundaries: dept44 misses a number inside percent-encoding, where it sits next to hex digits, and the
	// letter suffix of test personnummer (19900101TF03)
	private static final Pattern TWELVE_DIGIT_PERSON_NUMBER = Pattern.compile("(?:19|20)\\d{6}(?:%2[Dd]|[-+])?[0-9A-Za-z]{4}");
	private static final String REDACTED_PERSON_NUMBER = "[REDACTED-PNR]";

	// A partyId stays readable: it is opaque, and incidents are traced by it
	private static final Set<Category> PII_EXCEPT_UUID = EnumSet.of(PERSONAL_NUMBER, PHONE_NUMBER, EMAIL);

	// NEL and the Unicode line separators break lines too. Not LogUtils.sanitizeForLogging: it strips å, ä, ö and %
	private static final Pattern LINE_BREAKING_CHARACTER = Pattern.compile("[\\x00-\\x1F\\x7F\\u0085\\u2028\\u2029]");

	private static final String CAUSE_SEPARATOR = " | caused by: ";

	private LogSanitizer() {}

	public static String redact(final String text) {
		return ofNullable(text)
			.map(LogSanitizer::removeLineBreakingCharacters)
			.map(LogSanitizer::redactApiKey)
			.map(LogSanitizer::maskPersonalData)
			.orElse(null);
	}

	/**
	 * One redacted line for a throwable and its causes. Log this instead of the throwable, whose stack trace is not
	 * redacted.
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

	private static String removeLineBreakingCharacters(final String text) {
		return LINE_BREAKING_CHARACTER.matcher(text).replaceAll(" ");
	}

	private static String redactApiKey(final String text) {
		return API_KEY_PARAMETER.matcher(text).replaceAll(REDACTED_API_KEY);
	}

	private static String maskPersonalData(final String text) {
		final var withoutPersonNumbers = TWELVE_DIGIT_PERSON_NUMBER.matcher(text).replaceAll(REDACTED_PERSON_NUMBER);
		return PiiMasker.maskPii(withoutPersonNumbers, PII_EXCEPT_UUID);
	}
}

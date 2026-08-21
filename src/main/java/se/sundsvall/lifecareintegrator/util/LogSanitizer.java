package se.sundsvall.lifecareintegrator.util;

import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;

/**
 * Redaction of secrets and personal data from text that is about to be logged.
 *
 * <p>
 * Both Lifecare integrations authenticate with the {@code key} secret as a query parameter and select the citizen with
 * a {@code q=PersonId...} filter, so every Lifecare request URL carries an API key and a personnummer — and Lifecare
 * error bodies echo the filter back. Feign in turn builds its exception messages from the request URL
 * ({@code ... executing GET <url>}), which puts both in the log the moment such an exception is logged, defeating the
 * {@code Logger.Level.NONE} pinning the integration configurations rely on. Anything derived from a Lifecare request or
 * response must pass through here first.
 *
 * <p>
 * The {@code q} filter's <em>grammar</em> is deliberately preserved while its personnummer is replaced: whether the
 * request went out as {@code PersonId='…'} or as the rejected {@code PersonId:…} form is the single most useful thing
 * a failed EC call can tell us.
 */
public final class LogSanitizer {

	/** The API secret, as a query parameter. Redacted whole — none of it is diagnostic. */
	private static final Pattern SENSITIVE_QUERY_PARAM = Pattern.compile("(?i)([?&]key=)[^&\\s]*");

	/**
	 * A Swedish personnummer or samordningsnummer, with or without separator, in the twelve-digit form Lifecare uses.
	 * The last four characters are matched loosely because the test population uses letters there ({@code 19900101TF03}).
	 *
	 * <p>
	 * Deliberately unanchored by word boundaries: in a URL the number is surrounded by percent-encoded punctuation
	 * ({@code %3A19900101TF03}, {@code %2719900101TF03%27}) whose hex digits are themselves alphanumeric, so a boundary
	 * assertion silently stops matching exactly where it matters. Erring toward over-redaction is the safe direction.
	 */
	private static final Pattern PERSON_NUMBER = Pattern.compile("(?:19|20)[0-9]{6}(?:%2[Dd]|[-+])?[0-9A-Za-z]{4}");

	private static final String REDACTED_PARAM_VALUE = "$1[REDACTED]";

	private static final String REDACTED_PERSON_NUMBER = "[REDACTED-PNR]";

	private static final String CAUSE_SEPARATOR = " | caused by: ";

	private LogSanitizer() {}

	/**
	 * Replace the API key with {@code [REDACTED]} and every personnummer with {@code [REDACTED-PNR]}.
	 *
	 * @param  message the text to redact, may be {@code null}
	 * @return         the redacted text, or {@code null} if the message was {@code null}
	 */
	public static String redact(final String message) {
		return ofNullable(message)
			.map(text -> SENSITIVE_QUERY_PARAM.matcher(text).replaceAll(REDACTED_PARAM_VALUE))
			.map(text -> PERSON_NUMBER.matcher(text).replaceAll(REDACTED_PERSON_NUMBER))
			.orElse(null);
	}

	/**
	 * A redacted, single-line description of a throwable and its cause chain — type and message for each link. Used
	 * instead of logging the throwable itself, as the first line of a stack trace is the throwable's {@code toString()}
	 * and would reintroduce the unredacted message.
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

			current = current.getCause() == current ? null : current.getCause();
		}

		return description.toString();
	}
}

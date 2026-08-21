package se.sundsvall.lifecareintegrator.util;

import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;

/**
 * Redaction of secrets and personal data from text that is about to be logged.
 *
 * <p>
 * Both Lifecare integrations authenticate with the {@code key} secret as a query parameter and select the citizen with
 * a {@code q=PersonId...} filter, so every Lifecare request URL carries an API key and a personnummer. Feign builds its
 * exception messages from that URL ({@code ... executing GET <url>}), which puts both in the log the moment such an
 * exception is logged — defeating the {@code Logger.Level.NONE} pinning the integration configurations rely on.
 * Anything derived from a Lifecare request URL must pass through here first.
 */
public final class LogSanitizer {

	/**
	 * The query parameters that must never reach a log: {@code key} (the API secret) and {@code q} (the personnummer
	 * filter).
	 */
	private static final Pattern SENSITIVE_QUERY_PARAM = Pattern.compile("(?i)([?&](?:key|q)=)[^&\\s]*");

	private static final String REDACTED_VALUE = "$1[REDACTED]";

	private static final String CAUSE_SEPARATOR = " | caused by: ";

	private LogSanitizer() {}

	/**
	 * Replace the value of every sensitive query parameter with {@code [REDACTED]}.
	 *
	 * @param  message the text to redact, may be {@code null}
	 * @return         the redacted text, or {@code null} if the message was {@code null}
	 */
	public static String redact(final String message) {
		return ofNullable(message)
			.map(text -> SENSITIVE_QUERY_PARAM.matcher(text).replaceAll(REDACTED_VALUE))
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

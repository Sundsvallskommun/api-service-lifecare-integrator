package se.sundsvall.lifecareintegrator.integration.professionalweb;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/** Encodes form fields the way a browser posts them, for the sign-in forms and the prints' parameter questions. */
public final class ProfessionalWebForms {

	private ProfessionalWebForms() {}

	/**
	 * Encodes the fields in the order given, so a name may occur more than once as it does in a browser form.
	 *
	 * @param  fields the name and value of each field
	 * @return        the application/x-www-form-urlencoded body
	 */
	public static byte[] encode(final List<? extends Map.Entry<String, String>> fields) {
		return fields.stream()
			.map(field -> URLEncoder.encode(field.getKey(), UTF_8) + "=" + URLEncoder.encode(field.getValue(), UTF_8))
			.collect(Collectors.joining("&"))
			.getBytes(UTF_8);
	}
}
